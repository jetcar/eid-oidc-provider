package com.example.oidc.storage;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class OidcClient {
    private final String clientId;
    private final String clientSecret;
    private final List<String> redirectUris;
    private final String scope;

    public OidcClient(String clientId, String clientSecret, List<String> redirectUris, String scope) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUris = new ArrayList<>(redirectUris);
        this.scope = scope;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public List<String> getRedirectUris() {
        return Collections.unmodifiableList(redirectUris);
    }

    public String getRedirectUri(String incomingRedirectUri) {
        // Return the matching registered redirect URI if it exists
        if (redirectUris.contains(incomingRedirectUri)) {
            return incomingRedirectUri;
        }
        // If not found, return the first one as default (for backward compatibility)
        return redirectUris.isEmpty() ? null : redirectUris.get(0);
    }

    public boolean isValidRedirectUri(String uri) {
        return redirectUris.contains(uri);
    }

    public String getScope() {
        return scope;
    }
}