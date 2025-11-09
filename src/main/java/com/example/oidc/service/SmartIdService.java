package com.example.oidc.service;

import com.example.oidc.dto.SmartIdCheckResponse;
import com.example.oidc.dto.SmartIdSession;
import com.example.oidc.dto.SmartIdStartResponse;
import com.example.oidc.storage.OidcClient;
import com.example.oidc.storage.OidcClientRegistry;
import com.example.oidc.storage.OidcSessionStore;
import com.example.oidc.storage.UserInfo;

import ee.sk.smartid.AuthenticationHash;
import ee.sk.smartid.AuthenticationIdentity;
import ee.sk.smartid.AuthenticationResponseValidator;
import ee.sk.smartid.HashType;
import ee.sk.smartid.SmartIdClient;
import ee.sk.smartid.SmartIdAuthenticationResponse;
import ee.sk.smartid.rest.dao.SemanticsIdentifier;
import ee.sk.smartid.rest.dao.Interaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collections;
import com.example.oidc.util.RandomCodeGenerator;

@Service
public class SmartIdService {

    private final OidcSessionStore oidcSessionStore;
    private final OidcClientRegistry clientRegistry;
    private final SmartIdClient smartIdClient;
    private final AuthenticationResponseValidator authenticationResponseValidator;

    @Value("${smartid.client.allowed-interaction-text:Log in to self-service?}")
    private String allowedInteractionText;

    @Value("${smartid.client.certificate-level:QUALIFIED}")
    private String certificateLevel;

    @Autowired
    public SmartIdService(
            OidcSessionStore oidcSessionStore,
            OidcClientRegistry clientRegistry,
            SmartIdClient smartIdClient,
            @Qualifier("authenticationResponseValidator") AuthenticationResponseValidator authenticationResponseValidator) {
        this.oidcSessionStore = oidcSessionStore;
        this.clientRegistry = clientRegistry;
        this.smartIdClient = smartIdClient;
        this.authenticationResponseValidator = authenticationResponseValidator;
    }

    public SmartIdStartResponse startSmartId(String country, String personalCode) {
        // For security reasons a new hash value must be created for each new
        // authentication request

        AuthenticationHash authenticationHash = AuthenticationHash.generateRandomHash(HashType.SHA512);
        String verificationCode = authenticationHash.calculateVerificationCode();

        String sessionId = RandomCodeGenerator.generateRandomCode();

        oidcSessionStore.storeSmartIdSession(sessionId,
                new SmartIdSession(false, country, personalCode, authenticationHash.getHashInBase64()));

        SmartIdStartResponse responseBody = new SmartIdStartResponse();
        responseBody.sessionId = sessionId;
        responseBody.code = verificationCode;
        responseBody.country = country;
        return responseBody;
    }

    public SmartIdCheckResponse checkSmartId(String sessionId, String clientId, String redirectUri, String responseType,
            String scope, String state, String nonce) {
        SmartIdSession session = oidcSessionStore.getSmartIdSession(sessionId);
        SmartIdCheckResponse response = new SmartIdCheckResponse();
        response.sessionId = sessionId;

        if (session == null) {
            response.complete = false;
            response.validClient = false;
            response.authorized = false;
            response.error = "Session not found";
            return response;
        }

        boolean complete = false;
        boolean validClient = clientId != null && clientRegistry.isValidClient(clientId, redirectUri) != null;
        boolean authorized = false;
        OidcClient client = null;
        AuthenticationIdentity authIdentity = null;

        try {
            // Map country names to ISO codes
            String countryCode;
            switch (session.getCountry().toLowerCase()) {
                case "estonia":
                    countryCode = "EE";
                    break;
                case "latvia":
                    countryCode = "LV";
                    break;
                case "lithuania":
                    countryCode = "LT";
                    break;
                default:
                    countryCode = session.getCountry().toUpperCase();
            }

            SemanticsIdentifier semanticsIdentifier = new SemanticsIdentifier(
                    SemanticsIdentifier.IdentityType.PNO,
                    SemanticsIdentifier.CountryCode.valueOf(countryCode),
                    session.getPersonalCode());

            AuthenticationHash authenticationHash = new AuthenticationHash();
            authenticationHash.setHashInBase64(session.getAuthenticationHash());
            authenticationHash.setHashType(HashType.SHA512);

            SmartIdAuthenticationResponse smartIdresponse = smartIdClient
                    .createAuthentication()
                    .withSemanticsIdentifier(semanticsIdentifier)
                    .withAuthenticationHash(authenticationHash)
                    .withCertificateLevel(certificateLevel)
                    .withAllowedInteractionsOrder(
                            Collections.singletonList(Interaction.displayTextAndPIN(allowedInteractionText)))
                    .withShareMdClientIpAddress(true)
                    .authenticate();

            // Use injected singleton validator
            authIdentity = authenticationResponseValidator.validate(smartIdresponse);

            complete = authIdentity != null && authIdentity.getAuthCertificate() != null;
            validClient = clientId != null && clientRegistry.isValidClient(clientId, redirectUri) != null;
            authorized = complete;

            response.complete = complete;
            response.validClient = validClient;
            response.authorized = authorized;

            if (authorized && validClient && session != null) {
                client = clientRegistry.getClient(clientId);
                if (client != null) {
                    String code = RandomCodeGenerator.generateRandomCode();
                    UserInfo user = new UserInfo(
                            session.getPersonalCode(),
                            authIdentity.getGivenName(),
                            authIdentity.getSurname(),
                            session.getCountry(),
                            authIdentity.getDateOfBirth().get(),
                            null,
                            nonce);

                    String base64 = getCertificateBase64(authIdentity.getAuthCertificate());
                    user.setCert(base64); // set base64 certificate

                    oidcSessionStore.storeCode(code, user);
                    StringBuilder redirectUrl = new StringBuilder();
                    redirectUrl.append(client.getRedirectUri(redirectUri)).append("?code=").append(code);
                    if (state != null) {
                        redirectUrl.append("&state=").append(state);
                    }
                    response.redirectUrl = redirectUrl.toString();
                }
            }
        } catch (Exception e) {
            response.complete = false;
            response.validClient = false;
            response.authorized = false;
            response.error = e.getMessage();
            return response;
        }

        return response;
    }

    public static String getCertificateBase64(X509Certificate identity) {
        if (identity == null) {
            return null;
        }
        try {
            return Base64.getEncoder().encodeToString(identity.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode X509Certificate to Base64", e);
        }
    }

}
