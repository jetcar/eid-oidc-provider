package com.example.oidc.controllers.OIDC;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.oidc.storage.OidcClientRegistry;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.id.ClientID;
import java.net.URI;
import java.util.Map;
import java.util.HashMap;

@RestController
public class OidcAuthorizationController {
    private final OidcClientRegistry clientRegistry;

    @org.springframework.beans.factory.annotation.Autowired
    public OidcAuthorizationController(OidcClientRegistry clientRegistry) {
        this.clientRegistry = clientRegistry;
    }

    // /authorize is called by an external OIDC client (browser redirect or direct
    // link) to start the flow.
    // Example:
    // https://localhost:8443/authorize?response_type=code&client_id=demo-client-id&redirect_uri=https%3A%2F%2Flocalhost%3A8082%2Flogin%2Foauth2%2Fcode%2Fdemo&scope=openid%20profile%20email&state=xyz&nonce=abc
    // After validation it redirects to /index.html with the same query params; the
    // frontend reads them via getOidcParams().
    @GetMapping("/authorize")
    public org.springframework.web.servlet.view.RedirectView authorize(
            @RequestParam Map<String, String> params,
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-Forwarded-Prefix", required = false) String forwardedPrefix) {
        // Convert params to Map<String, List<String>>
        Map<String, java.util.List<String>> multiParams = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            multiParams.put(entry.getKey(), java.util.Collections.singletonList(entry.getValue()));
        }
        // Parse the OIDC authorization request
        AuthorizationRequest request;
        try {
            request = AuthorizationRequest.parse(URI.create("/authorize"), multiParams);
        } catch (com.nimbusds.oauth2.sdk.ParseException e) {
            return new org.springframework.web.servlet.view.RedirectView("/error?error=Invalid+authorization+request");
        }
        ClientID clientID = request.getClientID();
        URI redirectUri = request.getRedirectionURI();
        // Validate client (client_id and redirect_uri)
        var validClient = clientRegistry.isValidClient(clientID.getValue(), redirectUri.toString());
        if (clientID == null || redirectUri == null || validClient == null) {
            return new org.springframework.web.servlet.view.RedirectView("/error?error=Invalid+client_id");
        }
        // Use X-Forwarded-Prefix from HAProxy, or empty string if running directly
        String basePath = (forwardedPrefix != null && !forwardedPrefix.isEmpty()) ? forwardedPrefix : "";
        // Redirect to index.html with all OIDC parameters
        StringBuilder redirectUrl = new StringBuilder(basePath).append("/index.html?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            redirectUrl.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        if (redirectUrl.charAt(redirectUrl.length() - 1) == '&') {
            redirectUrl.setLength(redirectUrl.length() - 1);
        }
        return new org.springframework.web.servlet.view.RedirectView(redirectUrl.toString());
    }
}
