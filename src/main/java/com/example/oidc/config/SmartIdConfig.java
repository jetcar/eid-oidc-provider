package com.example.oidc.config;

import ee.sk.smartid.AuthenticationResponseValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;

@Configuration
public class SmartIdConfig {

    private static final Logger log = LoggerFactory.getLogger(SmartIdConfig.class);

    @Value("${smartid.client.trust-store}")
    private String trustStorePath;

    @Value("${smartid.client.trust-store-password:}")
    private String trustStorePassword;

    @Bean
    @Primary
    public AuthenticationResponseValidator authenticationResponseValidator() {
        try {
            AuthenticationResponseValidator validator = new AuthenticationResponseValidator();

            // Load all certificates from the trust store and add as trusted CAs
            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (InputStream is = new FileSystemResource(trustStorePath).getInputStream()) {
                ks.load(is, trustStorePassword != null ? trustStorePassword.toCharArray() : new char[0]);
            }
            for (String alias : Collections.list(ks.aliases())) {
                Certificate cert = ks.getCertificate(alias);
                if (cert instanceof X509Certificate certificate) {
                    log.info("Loaded Smart-ID CA certificate from trust-store '{}' alias='{}' DN='{}'",
                            trustStorePath, alias, certificate.getSubjectX500Principal().getName());
                    validator.addTrustedCACertificate(certificate);
                }
            }

            return validator;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Smart-ID CA certificates from smartid.client.trust-store", e);
        }
    }
}