package com.example.oidc.config;

import ee.sk.mid.MidAuthenticationResponseValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;
import java.security.KeyStore;

@Configuration
public class MobileIdValidatorConfig {

    @Value("${mid.client.trust-store}")
    private String trustStorePath;

    @Value("${mid.client.trust-store-password}")
    private String trustStorePassword;

    @Bean
    public MidAuthenticationResponseValidator midAuthenticationResponseValidator() {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (InputStream is = new FileSystemResource(trustStorePath).getInputStream()) {
                ks.load(is, trustStorePassword != null ? trustStorePassword.toCharArray() : new char[0]);
            }
            return new MidAuthenticationResponseValidator(ks);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Mobile-ID CA certificates from mid.client.trust-store", e);
        }
    }
}
