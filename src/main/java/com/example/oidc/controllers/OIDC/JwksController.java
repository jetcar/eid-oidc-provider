package com.example.oidc.controllers.OIDC;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.JWKSet;

@RestController
public class JwksController {
    private final JWKSet jwkSet;

    @Value("${server.ssl.key-store-password}")
    private String keystorePassword;

    public JwksController(@Value("${server.ssl.key-store}") String keystorePath,
            @Value("${server.ssl.key-store-password}") String keystorePassword) throws Exception {
        // Load public key from keystore.p12 (alias: springboot)
        java.security.KeyStore ks = java.security.KeyStore.getInstance("PKCS12");

        // Remove "file:" prefix if present
        String path = keystorePath.startsWith("file:") ? keystorePath.substring(5) : keystorePath;

        try (java.io.InputStream is = new FileSystemResource(path).getInputStream()) {
            ks.load(is, keystorePassword.toCharArray());
        }
        java.security.cert.Certificate cert = ks.getCertificate("springboot");
        java.security.interfaces.RSAPublicKey publicKey = (java.security.interfaces.RSAPublicKey) cert.getPublicKey();
        RSAKey rsaJwk = new RSAKey.Builder(publicKey)
                .keyID("springboot")
                .build();
        jwkSet = new JWKSet(rsaJwk);
    }

    @GetMapping("/.well-known/jwks.json")
    public String getJwks() {
        return jwkSet.toJSONObject().toString();
    }
}
