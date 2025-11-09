package com.example.oidc.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class OidcClientRegistry {
    private static final Logger logger = LoggerFactory.getLogger(OidcClientRegistry.class);
    private final Map<String, OidcClient> clients = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${CONFIG_PATH:/repo/vscode/backend/config}")
    private String configPath;

    public OidcClientRegistry(@Value("${CONFIG_PATH:/repo/vscode/backend/config}") String configPath) {
        this.configPath = configPath;
        loadClientsFromConfig();
    }

    private void loadClientsFromConfig() {
        File configFile = new File(configPath, "oidc-clients.json");

        logger.info("Loading OIDC clients from: {}", configFile.getAbsolutePath());

        if (!configFile.exists()) {
            String errorMsg = "OIDC clients config file not found: " + configFile.getAbsolutePath();
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        try {
            JsonNode root = objectMapper.readTree(configFile);
            JsonNode clientsArray = root.get("clients");

            if (clientsArray == null || !clientsArray.isArray()) {
                String errorMsg = "Invalid JSON format: 'clients' array not found in " + configFile.getAbsolutePath();
                logger.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            int index = 0;
            for (JsonNode clientNode : clientsArray) {
                String clientId = clientNode.has("clientId") ? clientNode.get("clientId").asText() : null;
                String clientSecret = clientNode.has("clientSecret") ? clientNode.get("clientSecret").asText() : null;
                String scope = clientNode.has("scope") ? clientNode.get("scope").asText() : null;

                // Parse redirectUri as either array or single string
                List<String> redirectUris = new ArrayList<>();
                if (clientNode.has("redirectUri")) {
                    JsonNode redirectUriNode = clientNode.get("redirectUri");
                    if (redirectUriNode.isArray()) {
                        for (JsonNode uriNode : redirectUriNode) {
                            redirectUris.add(uriNode.asText());
                        }
                    } else {
                        redirectUris.add(redirectUriNode.asText());
                    }
                }

                if (clientId != null && clientSecret != null && !redirectUris.isEmpty() && scope != null) {
                    OidcClient client = new OidcClient(clientId, clientSecret, redirectUris, scope);
                    clients.put(clientId, client);
                    logger.info("Loaded OIDC client [{}]: {} with redirect URIs: {}", index, clientId, redirectUris);
                } else {
                    logger.warn("Incomplete client configuration at index: {}", index);
                }
                index++;
            }

            if (clients.isEmpty()) {
                String errorMsg = "No valid clients found in config file: " + configFile.getAbsolutePath();
                logger.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            logger.info("Total clients loaded: {}", clients.size());

        } catch (IOException e) {
            String errorMsg = "Failed to load OIDC clients configuration: " + e.getMessage();
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    public OidcClient getClient(String clientId) {
        return clients.get(clientId);
    }

    public OidcClient isValidClient(String clientId, String redirectUri) {
        OidcClient client = clients.get(clientId);
        return (client != null && client.isValidRedirectUri(redirectUri)) ? client : null;
    }

    public OidcClient getClientByReturnUri(String returnUri) {
        for (OidcClient client : clients.values()) {
            if (client.isValidRedirectUri(returnUri)) {
                return client;
            }
        }
        return null;
    }
}
