package com.example.oidc.controllers;

import com.example.oidc.dto.SmartIdCheckResponse;
import com.example.oidc.dto.SmartIdStartResponse;
import com.example.oidc.service.ISmartIdService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Smart-ID Authentication", description = "Endpoints for Smart-ID authentication flow")
public class SmartIdController {

    private final ISmartIdService smartIdService;

    @Autowired
    public SmartIdController(ISmartIdService smartIdService) {
        this.smartIdService = smartIdService;
    }

    @PostMapping("/smartid/start")
    @Operation(summary = "Start Smart-ID authentication", description = "Initiates a Smart-ID authentication session")
    public SmartIdStartResponse startSmartId(
            @Parameter(description = "Country code (e.g., EE, LT, LV)", required = true) @RequestParam String country,
            @Parameter(description = "Personal identification code", required = true) @RequestParam String personalCode,
            @Parameter(description = "PKCE code challenge") @RequestParam(required = false) String code_challenge,
            @Parameter(description = "PKCE code challenge method (S256 or plain)") @RequestParam(required = false) String code_challenge_method) {
        return smartIdService.startSmartId(country, personalCode, code_challenge, code_challenge_method);
    }

    @GetMapping("/smartid/check")
    @Operation(summary = "Check Smart-ID authentication status", description = "Polls the status of an ongoing Smart-ID authentication session")
    public SmartIdCheckResponse checkSmartId(
            @Parameter(description = "Smart-ID session identifier", required = true) @RequestParam String sessionId,
            @Parameter(description = "OAuth2 client ID") @RequestParam(required = false) String client_id,
            @Parameter(description = "OAuth2 redirect URI") @RequestParam(required = false) String redirect_uri,
            @Parameter(description = "OAuth2 response type") @RequestParam(required = false) String response_type,
            @Parameter(description = "OAuth2 scope") @RequestParam(required = false) String scope,
            @Parameter(description = "OAuth2 state parameter") @RequestParam(required = false) String state,
            @Parameter(description = "OIDC nonce parameter") @RequestParam(required = false) String nonce) {
        return smartIdService.checkSmartId(sessionId, client_id, redirect_uri, response_type, scope, state, nonce);
    }
}
