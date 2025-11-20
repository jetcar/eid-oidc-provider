package com.example.oidc.controllers.OIDC;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.HashMap;

@RestController
@Tag(name = "OIDC Discovery", description = "OpenID Connect discovery endpoints")
public class OidcDiscoveryController {
    @GetMapping("/.well-known/openid-configuration")
    @Operation(summary = "OpenID Connect discovery", description = "Returns the OpenID Connect discovery document")
    public Map<String, Object> discovery(HttpServletRequest request) {
        // Build base URL from forwarded headers (set by HAProxy)
        String proto = request.getHeader("X-Forwarded-Proto");
        if (proto == null)
            proto = request.getScheme();

        String host = request.getHeader("X-Forwarded-Host");
        if (host == null)
            host = request.getServerName() + ":" + request.getServerPort();

        String prefix = request.getHeader("X-Forwarded-Prefix");
        if (prefix == null)
            prefix = "";

        String baseUrl = proto + "://" + host + prefix;

        Map<String, Object> config = new HashMap<>();
        config.put("issuer", baseUrl);
        config.put("authorization_endpoint", baseUrl + "/authorize");
        config.put("token_endpoint", baseUrl + "/token");
        config.put("userinfo_endpoint", baseUrl + "/userinfo");
        config.put("jwks_uri", baseUrl + "/.well-known/jwks.json");
        config.put("response_types_supported", new String[] { "code", "id_token", "token" });
        config.put("subject_types_supported", new String[] { "public" });
        config.put("id_token_signing_alg_values_supported", new String[] { "RS256" });
        config.put("scopes_supported", new String[] { "openid", "profile", "email" });
        config.put("token_endpoint_auth_methods_supported", new String[] { "client_secret_basic" });
        return config;
    }
}
