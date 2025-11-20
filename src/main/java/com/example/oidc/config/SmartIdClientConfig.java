package com.example.oidc.config;

import ee.sk.smartid.SmartIdClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import java.io.InputStream;
import java.security.KeyStore;

@Configuration
public class SmartIdClientConfig {

    @Value("${smartid.client.host-url}")
    private String smartIdClientHostUrl;

    @Value("${smartid.client.relying-party-uuid}")
    private String smartIdClientRelyingPartyUUID;

    @Value("${smartid.client.relying-party-name}")
    private String smartIdClientRelyingPartyName;

    @Value("${smartid.client.api-trust-store}")
    private String apiTrustStore;

    @Value("${smartid.client.api-trust-store-password:}")
    private String apiTrustStorePassword;

    @Bean
    public SmartIdClient smartIdClient() {
        try {
            KeyStore trustStoreInstance = KeyStore.getInstance("PKCS12");
            try (InputStream trustStoreStream = new FileSystemResource(apiTrustStore).getInputStream()) {
                trustStoreInstance.load(trustStoreStream,
                        apiTrustStorePassword != null ? apiTrustStorePassword.toCharArray() : new char[0]);
            }
            SmartIdClient client = new SmartIdClient();
            client.setRelyingPartyUUID(smartIdClientRelyingPartyUUID);
            client.setRelyingPartyName(smartIdClientRelyingPartyName);
            client.setHostUrl(smartIdClientHostUrl);
            client.setTrustStore(trustStoreInstance);
            return client;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SmartIdClient with trust store", e);
        }
    }
}
