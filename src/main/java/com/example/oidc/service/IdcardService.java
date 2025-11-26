package com.example.oidc.service;

import com.example.oidc.dto.IdCardChallengeResponse;
import com.example.oidc.dto.IdCardLoginRequest;
import com.example.oidc.dto.IdCardLoginResponse;
import com.example.oidc.dto.IdCardSession;
import com.example.oidc.storage.OidcClient;
import com.example.oidc.storage.OidcClientRegistry;
import com.example.oidc.storage.IOidcSessionStore;
import com.example.oidc.storage.UserInfo;
import com.example.oidc.util.PersonalCodeHelper;
import com.example.oidc.util.RandomCodeGenerator;
import eu.webeid.security.certificate.CertificateData;
import eu.webeid.security.challenge.ChallengeNonce;
import eu.webeid.security.challenge.ChallengeNonceGenerator;
import eu.webeid.security.validator.AuthTokenValidator;
import eu.webeid.security.exceptions.AuthTokenException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IdcardService implements IIdcardService {

    private final ChallengeNonceGenerator challengeNonceGenerator;
    private final AuthTokenValidator authTokenValidator;
    private final OidcClientRegistry clientRegistry;
    private final IOidcSessionStore oidcSessionStore;

    @Autowired
    public IdcardService(
            ChallengeNonceGenerator challengeNonceGenerator,
            AuthTokenValidator authTokenValidator,
            OidcClientRegistry clientRegistry,
            IOidcSessionStore oidcSessionStore) {
        this.challengeNonceGenerator = challengeNonceGenerator;
        this.authTokenValidator = authTokenValidator;
        this.clientRegistry = clientRegistry;
        this.oidcSessionStore = oidcSessionStore;
    }

    @Override
    public IdCardChallengeResponse createChallenge(
            String clientId,
            String redirectUri,
            String state,
            String nonce) {
        ChallengeNonce challengeNonce = challengeNonceGenerator.generateAndStoreNonce();
        IdCardChallengeResponse resp = new IdCardChallengeResponse();
        String sessionId = RandomCodeGenerator.generateRandomCode();
        oidcSessionStore.storeIdCardSession(
                sessionId,
                new IdCardSession(false, challengeNonce.getBase64EncodedNonce()));
        resp.nonce = challengeNonce.getBase64EncodedNonce();
        resp.sessionId = sessionId;
        return resp;
    }

    @Override
    public IdCardLoginResponse login(
            IdCardLoginRequest body,
            String clientId,
            String redirectUri,
            String state,
            String nonce,
            String sessionId) {
        IdCardLoginResponse resp = new IdCardLoginResponse();
        Object authTokenObj = body.getAuthToken();
        eu.webeid.security.authtoken.WebEidAuthToken authToken = null;
        if (authTokenObj instanceof Map) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String json = mapper.writeValueAsString(authTokenObj);
                authToken = mapper.readValue(json, eu.webeid.security.authtoken.WebEidAuthToken.class);
            } catch (Exception e) {
                resp.error = "Invalid authToken format";
                resp.message = e.getMessage();
                return resp;
            }
        }
        if (authToken == null) {
            resp.error = "Missing or invalid authToken";
            return resp;
        }

        IdCardSession idCardSession = null;
        if (sessionId != null && !sessionId.isBlank()) {
            idCardSession = oidcSessionStore.getIdCardSession(sessionId);
            if (idCardSession == null) {
                resp.error = "Session not found or expired";
                return resp;
            }
        }

        X509Certificate certificate;
        try {
            certificate = authTokenValidator.validate(authToken, idCardSession.getChallengeNonce());
        } catch (AuthTokenException e) {
            resp.error = "Web eID token validation failed";
            resp.message = e.getMessage();
            return resp;
        } catch (Exception e) {
            resp.error = "Unexpected error";
            resp.message = e.getMessage();
            return resp;
        }
        Optional<String> country;
        Optional<String> subject;
        Optional<String> givenName;
        Optional<String> surname;
        LocalDate dateOfBirth;
        try {
            country = CertificateData.getSubjectCountryCode(certificate);
            Optional<String> rawSubject = CertificateData.getSubjectIdCode(certificate);
            subject = rawSubject.map(s -> s.replaceAll("PNO", "").replaceAll(country.orElse("") + "-", ""));
            givenName = CertificateData.getSubjectGivenName(certificate);
            surname = CertificateData.getSubjectSurname(certificate);
            dateOfBirth = PersonalCodeHelper.getDateOfBirth(subject.get());
        } catch (CertificateEncodingException e) {
            resp.error = "Invalid certificate";
            return resp;
        }

        String certBase64;
        try {
            certBase64 = java.util.Base64.getEncoder().encodeToString(certificate.getEncoded());
        } catch (Exception e) {
            resp.error = "Failed to encode certificate";
            resp.message = e.getMessage();
            return resp;
        }

        OidcClient client = clientRegistry.isValidClient(clientId, redirectUri);
        if (client == null) {
            resp.error = "Invalid client or redirect_uri";
            return resp;
        }

        // Replace UUID with secure random code
        String code = RandomCodeGenerator.generateRandomCode();
        UserInfo user = new UserInfo(
                subject.get(),
                givenName.get(),
                surname.get(),
                country.get(),
                dateOfBirth,
                null,
                nonce);
        user.setCert(certBase64);
        oidcSessionStore.storeCode(code, user);

        StringBuilder redirect = new StringBuilder(client.getRedirectUri(redirectUri))
                .append("?code=").append(code);
        if (state != null) {
            redirect.append("&state=").append(state);
        }
        resp.redirectUrl = redirect.toString();
        return resp;
    }
}
