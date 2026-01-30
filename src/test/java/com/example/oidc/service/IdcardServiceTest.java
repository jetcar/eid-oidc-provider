package com.example.oidc.service;

import com.example.oidc.dto.IdCardChallengeResponse;
import com.example.oidc.dto.IdCardLoginRequest;
import com.example.oidc.dto.IdCardLoginResponse;
import com.example.oidc.dto.IdCardSession;
import com.example.oidc.storage.OidcClient;
import com.example.oidc.storage.OidcClientRegistry;
import com.example.oidc.storage.OidcSessionStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.webeid.security.authtoken.WebEidAuthToken;
import eu.webeid.security.certificate.CertificateData;
import eu.webeid.security.challenge.ChallengeNonce;
import eu.webeid.security.challenge.ChallengeNonceGenerator;
import eu.webeid.security.validator.AuthTokenValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IdcardServiceTest {

        private ChallengeNonceGenerator challengeNonceGenerator;
        private AuthTokenValidator authTokenValidator;
        private OidcClientRegistry clientRegistry;
        private OidcSessionStore oidcSessionStore;
        private IdcardService idcardService;

        @BeforeEach
        void setUp() {
                challengeNonceGenerator = mock(ChallengeNonceGenerator.class);
                authTokenValidator = mock(AuthTokenValidator.class);
                clientRegistry = mock(OidcClientRegistry.class);
                oidcSessionStore = mock(OidcSessionStore.class);
                idcardService = new IdcardService(
                                challengeNonceGenerator,
                                authTokenValidator,
                                clientRegistry,
                                oidcSessionStore);
        }

        @Test
        void testCreateChallengeReturnsNonceAndSessionId() {
                ChallengeNonce mockNonce = mock(ChallengeNonce.class);
                when(challengeNonceGenerator.generateAndStoreNonce()).thenReturn(mockNonce);
                when(mockNonce.getBase64EncodedNonce()).thenReturn("test-nonce");

                IdCardChallengeResponse result = idcardService.createChallenge("client1", "http://localhost", "state1",
                                "nonce1", "codeChallenge1", "S256");
                assertNotNull(result.nonce);
                assertNotNull(result.sessionId);
                assertEquals("test-nonce", result.nonce);
        }

        @Test
        void testLoginMissingAuthToken() {
                IdCardLoginRequest body = new IdCardLoginRequest();
                IdCardLoginResponse response = idcardService.login(body, "client1", "http://localhost", "state1",
                                "nonce1", "session1");
                assertEquals("Missing or invalid authToken", response.error);
        }

        @Test
        void testLoginSessionNotFound() {
                IdCardLoginRequest body = new IdCardLoginRequest();
                body.setAuthToken(Map.of("dummy", "value"));
                when(oidcSessionStore.getIdCardSession("session1")).thenReturn(null);
                IdCardLoginResponse response = idcardService.login(body, "client1", "http://localhost", "state1",
                                "nonce1", "session1");
                assertEquals("Session not found or expired", response.error);
        }

        @Test
        void testLoginSuccess() throws Exception {
                // Prepare mock certificate and token
                WebEidAuthToken mockToken = new WebEidAuthToken();
                mockToken.setAlgorithm("testAlgorithm");
                mockToken.setSignature("testSignature");
                mockToken.setFormat("testFormat");
                mockToken.setUnverifiedCertificate("testCertificate");

                IdCardLoginRequest body = new IdCardLoginRequest();
                // Serialize mockToken as Map to simulate real request body
                ObjectMapper mapper = new ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> tokenMap = mapper.convertValue(mockToken, Map.class);
                body.setAuthToken(tokenMap);

                IdCardSession mockSession = mock(IdCardSession.class);
                when(mockSession.getChallengeNonce()).thenReturn("challenge-nonce");
                when(oidcSessionStore.getIdCardSession("session1")).thenReturn(mockSession);

                IdcardService service = new IdcardService(
                                challengeNonceGenerator,
                                authTokenValidator,
                                clientRegistry,
                                oidcSessionStore);

                X509Certificate mockCert = mock(X509Certificate.class);
                when(authTokenValidator.validate(any(WebEidAuthToken.class), any())).thenReturn(mockCert);

                // Mock certificate data
                when(mockCert.getEncoded()).thenReturn(new byte[] { 1, 2, 3 });
                mockStatic(CertificateData.class);
                when(CertificateData.getSubjectCountryCode(mockCert)).thenReturn(Optional.of("EE"));
                when(CertificateData.getSubjectIdCode(mockCert)).thenReturn(Optional.of("PNOEE-40404040009"));
                when(CertificateData.getSubjectGivenName(mockCert)).thenReturn(Optional.of("John"));
                when(CertificateData.getSubjectSurname(mockCert)).thenReturn(Optional.of("Smith"));

                OidcClient mockClient = mock(OidcClient.class);
                when(mockClient.getRedirectUri(any())).thenReturn("http://localhost");
                when(clientRegistry.isValidClient(any(), any())).thenReturn(mockClient);

                IdCardLoginResponse response = service.login(body, "client1", "http://localhost", "state1", "nonce1",
                                "session1");
                assertNotNull(response.redirectUrl);
                assertTrue(response.redirectUrl.contains("http://localhost"));
                assertNull(response.error);
        }
}