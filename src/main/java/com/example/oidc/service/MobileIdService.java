package com.example.oidc.service;

import com.example.oidc.dto.MobileIdCheckResponse;
import com.example.oidc.dto.MobileIdSession;
import com.example.oidc.dto.MobileIdStartResponse;
import com.example.oidc.storage.OidcClient;
import com.example.oidc.storage.OidcClientRegistry;
import com.example.oidc.storage.OidcSessionStore;
import com.example.oidc.storage.UserInfo;
import com.example.oidc.util.PersonalCodeHelper;
import com.example.oidc.util.RandomCodeGenerator;
import ee.sk.mid.MidAuthenticationHashToSign;
import ee.sk.mid.MidAuthenticationIdentity;
import ee.sk.mid.MidAuthenticationResponseValidator;
import ee.sk.mid.MidAuthenticationResult;
import ee.sk.mid.MidClient;
import ee.sk.mid.MidHashType;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.mid.MidAuthentication;
import ee.sk.mid.rest.dao.request.MidAuthenticationRequest;
import ee.sk.mid.rest.dao.response.MidAuthenticationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class MobileIdService {

    private static final Logger log = LoggerFactory.getLogger(MobileIdService.class);

    private final MidClient midClient;
    private final OidcSessionStore oidcSessionStore;
    private final OidcClientRegistry clientRegistry;
    private final MidAuthenticationResponseValidator authenticationResponseValidator;

    @Autowired
    public MobileIdService(
            MidClient midClient,
            OidcSessionStore oidcSessionStore,
            OidcClientRegistry clientRegistry,
            @Qualifier("midAuthenticationResponseValidator") MidAuthenticationResponseValidator authenticationResponseValidator) {
        this.midClient = midClient;
        this.oidcSessionStore = oidcSessionStore;
        this.clientRegistry = clientRegistry;
        this.authenticationResponseValidator = authenticationResponseValidator;
    }

    public MobileIdStartResponse startMobileId(String personalCode, String phoneNumber, String countryCode,
            String clientId,
            String redirectUri) {
        OidcClient client = clientRegistry.isValidClient(clientId, redirectUri);
        if (client == null) {
            MobileIdStartResponse errorResponse = new MobileIdStartResponse();
            errorResponse.sessionId = null;
            errorResponse.code = null;
            return errorResponse;
        }
        // Add country code to phone number if not present
        String fullPhoneNumber = phoneNumber;
        if (countryCode != null && !phoneNumber.startsWith(countryCode)) {
            // Remove any leading country code
            for (String code : new String[] { "+372", "+371", "+370" }) {
                if (fullPhoneNumber.startsWith(code)) {
                    fullPhoneNumber = fullPhoneNumber.substring(code.length());
                    break;
                }
            }
            fullPhoneNumber = countryCode + fullPhoneNumber.replaceFirst("^\\+", "");
        }

        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();
        String verificationCode = authenticationHash.calculateVerificationCode();

        MidAuthenticationRequest request = MidAuthenticationRequest.newBuilder()
                .withPhoneNumber(fullPhoneNumber)
                .withNationalIdentityNumber(personalCode)
                .withHashToSign(authenticationHash)
                .withLanguage(ee.sk.mid.MidLanguage.ENG)
                .build();

        MidAuthenticationResponse response = midClient.getMobileIdConnector().authenticate(request);

        String sessionId = response.getSessionID();
        log.info("Authentication session ID: " + sessionId);

        oidcSessionStore.storeMobileIdSession(sessionId,
                new MobileIdSession(false, personalCode, fullPhoneNumber, authenticationHash.getHashInBase64()));

        MobileIdStartResponse responseBody = new MobileIdStartResponse();
        responseBody.sessionId = sessionId;
        responseBody.code = verificationCode;
        return responseBody;
    }

    public MobileIdCheckResponse checkMobileId(String sessionId, String clientId, String redirectUri,
            String responseType,
            String scope, String state, String nonce) {
        MobileIdSession session = oidcSessionStore.getMobileIdSession(sessionId);
        MobileIdCheckResponse response = new MobileIdCheckResponse();
        response.sessionId = sessionId;

        boolean complete = false;
        OidcClient client = clientRegistry.isValidClient(clientId, redirectUri);
        boolean validClient = client != null;
        boolean authorized = false;
        String error = null;
        MidAuthenticationResult authenticationResult = null;
        MidAuthentication authentication = null;

        if (!validClient) {
            response.complete = complete;
            response.validClient = false;
            response.authorized = authorized;
            response.error = "Invalid client";
            return response;
        }

        if (session != null) {
            try {
                MidSessionStatus sessionStatus = midClient.getSessionStatusPoller()
                        .fetchFinalSessionStatus(sessionId, "/authentication/session/{sessionId}");

                MidAuthenticationHashToSign authenticationHashToSign = MidAuthenticationHashToSign.newBuilder()
                        .withHashType(MidHashType.SHA256)
                        .withHashInBase64(session.getAuthenticationHash())
                        .build();

                authentication = midClient.createMobileIdAuthentication(sessionStatus,
                        authenticationHashToSign);

                // Use injected singleton validator
                authenticationResult = authenticationResponseValidator.validate(authentication);

                if (authenticationResult.isValid()) {
                    complete = true;
                    authorized = true;
                } else {
                    error = String.valueOf(authenticationResult.getErrors());
                    log.error("MobileId authentication errors for sessionId {}: {}", sessionId,
                            authenticationResult.getErrors());
                }
            } catch (Exception e) {
                error = e.getMessage();
                log.error("Error polling MobileId status for sessionId {}: {}", sessionId, e.getMessage());
            }
        } else {
            error = "Session not found";
        }

        response.complete = complete;
        response.validClient = validClient;
        response.authorized = authorized;
        if (error != null) {
            response.error = error;
        }

        if (authorized && validClient && session != null && client != null && authenticationResult != null) {
            MidAuthenticationIdentity identityUser = authenticationResult.getAuthenticationIdentity();
            String code = RandomCodeGenerator.generateRandomCode();
            UserInfo user = new UserInfo(
                    identityUser.getIdentityCode(),
                    identityUser.getGivenName(),
                    identityUser.getSurName(),
                    identityUser.getCountry(),
                    PersonalCodeHelper.getDateOfBirth(identityUser.getIdentityCode()),
                    session.getPhoneNumber(),
                    nonce);

            // Add base64-encoded certificate to userinfo
            if (authentication.getCertificate() != null) {
                try {
                    String certBase64 = java.util.Base64.getEncoder()
                            .encodeToString(authentication.getCertificate().getEncoded());
                    user.setCert(certBase64);
                } catch (Exception e) {
                    log.error("Failed to encode certificate for sessionId {}: {}", sessionId, e.getMessage());
                }
            }

            oidcSessionStore.storeCode(code, user);
            StringBuilder redirectUrl = new StringBuilder()
                    .append(client.getRedirectUri(redirectUri)).append("?code=").append(code);
            if (state != null) {
                redirectUrl.append("&state=").append(state);
            }
            response.redirectUrl = redirectUrl.toString();
        }
        return response;
    }
}