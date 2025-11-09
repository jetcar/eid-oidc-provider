package com.example.oidc.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class DebugController {

    private final Environment environment;

    @Value("${spring.redis.host:NOT_SET}")
    private String redisHost;

    @Value("${spring.redis.port:NOT_SET}")
    private String redisPort;

    @Value("${server.port:NOT_SET}")
    private String serverPort;

    @Value("${server.ssl.enabled:NOT_SET}")
    private String sslEnabled;

    public DebugController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/debug/config")
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();

        // Redis configuration
        config.put("spring.redis.host", redisHost);
        config.put("spring.redis.port", redisPort);

        // Server configuration
        config.put("server.port", serverPort);
        config.put("server.ssl.enabled", sslEnabled);

        // Environment variables
        Map<String, String> envVars = new HashMap<>();
        envVars.put("REDIS_HOST", System.getenv("REDIS_HOST"));
        envVars.put("SERVER_PORT", System.getenv("SERVER_PORT"));
        envVars.put("SSL_ENABLED", System.getenv("SSL_ENABLED"));
        config.put("environment_variables", envVars);

        // All active profiles
        config.put("active_profiles", environment.getActiveProfiles());

        return config;
    }
}
