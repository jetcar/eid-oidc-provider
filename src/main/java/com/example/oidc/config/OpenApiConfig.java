package com.example.oidc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI backendOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OIDC Backend API")
                        .description("OpenID Connect Provider with Mobile-ID, Smart-ID and ID-Card authentication")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@example.com")))
                .servers(List.of(
                        new Server().url("https://localhost:8443").description("Direct HTTPS Server"),
                        new Server().url("https://localhost/backend").description("Via HAProxy")));
    }
}
