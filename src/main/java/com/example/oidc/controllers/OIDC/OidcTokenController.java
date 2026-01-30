package com.example.oidc.controllers.OIDC;

import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.oidc.storage.UserInfo;
import com.example.oidc.storage.OidcClient;
import com.example.oidc.storage.OidcClientRegistry;
import com.example.oidc.storage.IOidcSessionStore;
import com.example.oidc.dto.PkceData;
import com.example.oidc.util.PkceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import java.security.KeyStore;
import java.security.PrivateKey;

@RestController
public class OidcTokenController {
    private final IOidcSessionStore oidcSessionStore;
    private final OidcClientRegistry clientRegistry;
    private PrivateKey jwtPrivateKey;
    @Value("${server.ssl.key-store}")
    private String keystorePath;
    @Value("${server.ssl.key-store-password}")
    private String keystorePassword;
    @Value("${oidc.issuer:https://localhost:8443}")
    private String issuer;

    @Autowired
    public OidcTokenController(IOidcSessionStore oidcSessionStore, OidcClientRegistry clientRegistry) {
        this.oidcSessionStore = oidcSessionStore;
        this.clientRegistry = clientRegistry;
    }

    @jakarta.annotation.PostConstruct
    public void loadPrivateKey() throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");

        // Remove "file:" prefix if present
        String path = keystorePath.startsWith("file:") ? keystorePath.substring(5) : keystorePath;

        try (java.io.InputStream is = new FileSystemResource(path).getInputStream()) {
            ks.load(is, keystorePassword.toCharArray());
        }
        jwtPrivateKey = (PrivateKey) ks.getKey("springboot", keystorePassword.toCharArray());
    }

    @PostMapping("/token")
    public Map<String, Object> token(
            @RequestParam("code") String code,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam(value = "scope", required = false, defaultValue = "openid profile email") String scope,
            @RequestParam(value = "code_verifier", required = false) String codeVerifier,
            HttpServletResponse servletResponse) {
        Map<String, Object> response = new HashMap<>();
        if (code == null || code.isEmpty()) {
            response.put("error", "Missing authorization code");
            return response;
        }
        String clientId = null;
        if (redirectUri != null && !redirectUri.isEmpty()) {
            OidcClient client = clientRegistry.getClientByReturnUri(redirectUri);
            if (client != null) {
                clientId = client.getClientId();
            }
        }
        if (clientId == null || clientId.isEmpty()) {
            response.put("error", "Missing client_id parameter and could not resolve from redirect_uri");
            return response;
        }

        // PKCE validation: retrieve stored PKCE data
        PkceData pkceData = oidcSessionStore.getPkceDataByCode(code);
        if (pkceData != null) {
            // PKCE was used during authorization - code_verifier is required
            if (codeVerifier == null || codeVerifier.isEmpty()) {
                response.put("error", "invalid_grant");
                response.put("error_description", "code_verifier required for PKCE");
                return response;
            }

            // Validate code_verifier against stored code_challenge
            boolean valid = PkceValidator.validate(
                    codeVerifier,
                    pkceData.getCodeChallenge(),
                    pkceData.getCodeChallengeMethod());

            if (!valid) {
                response.put("error", "invalid_grant");
                response.put("error_description", "Invalid code_verifier");
                return response;
            }
        }

        UserInfo user = oidcSessionStore.getUserByCode(code);
        if (user == null) {
            response.put("error", "Invalid or expired authorization code");
            return response;
        }
        // Generate a proper JWT for access_token
        String accessToken = null;
        try {
            JWTClaimsSet accessTokenClaims = new JWTClaimsSet.Builder()
                    .issuer(issuer)
                    .subject(user.getSub())
                    .audience(clientId)
                    .claim("scope", scope)
                    .expirationTime(new java.util.Date(System.currentTimeMillis() + 3600 * 1000))
                    .issueTime(new java.util.Date())
                    .build();
            JWSHeader accessTokenHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID("springboot")
                    .build();
            SignedJWT accessSignedJWT = new SignedJWT(accessTokenHeader, accessTokenClaims);
            accessSignedJWT.sign(new RSASSASigner(jwtPrivateKey));
            accessToken = accessSignedJWT.serialize();
            oidcSessionStore.storeToken(accessToken, user);

            // Generate a real JWT for id_token
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .issuer(issuer)
                    .subject(user.getSub())
                    .audience(clientId)
                    .claim("name", user.getGivenName())
                    .claim("surname", user.getSurname())
                    .claim("country", user.getCountry())
                    .claim("phone_number", user.getPhoneNumber())
                    .expirationTime(new java.util.Date(System.currentTimeMillis() + 3600 * 1000))
                    .issueTime(new java.util.Date());

            if (user.getNonce() != null && !user.getNonce().isEmpty()) {
                claimsBuilder.claim("nonce", user.getNonce());
            }

            JWTClaimsSet claims = claimsBuilder.build();
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID("springboot")
                    .build();
            SignedJWT signedJWT = new SignedJWT(header, claims);
            signedJWT.sign(new RSASSASigner(jwtPrivateKey));
            String idToken = signedJWT.serialize();
            response.put("access_token", accessToken);
            response.put("token_type", "Bearer");
            response.put("expires_in", 3600);
            response.put("id_token", idToken);
            servletResponse.addHeader("Set-Cookie", "id_token=" + idToken + "; Path=/; HttpOnly; Secure");
        } catch (Exception e) {
            response.put("error", "Failed to generate id_token: " + e.getMessage());
        }
        return response;
    }
}
