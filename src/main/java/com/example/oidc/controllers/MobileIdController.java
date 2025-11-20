package com.example.oidc.controllers;

import com.example.oidc.dto.MobileIdCheckResponse;
import com.example.oidc.dto.MobileIdStartResponse;
import com.example.oidc.service.MobileIdService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Mobile-ID Authentication", description = "Endpoints for Mobile-ID authentication flow")
public class MobileIdController {

    private final MobileIdService mobileIdService;

    @Autowired
    public MobileIdController(MobileIdService mobileIdService) {
        this.mobileIdService = mobileIdService;
    }

    @PostMapping("/mobileid/start")
    @Operation(summary = "Start Mobile-ID authentication", description = "Initiates a Mobile-ID authentication session")
    public MobileIdStartResponse startMobileId(
            @Parameter(description = "Country code (e.g., EE, LT, LV)", required = true) @RequestParam String countryCode,
            @Parameter(description = "Personal identification code", required = true) @RequestParam String personalCode,
            @Parameter(description = "Phone number with country code", required = true) @RequestParam String phoneNumber,
            @Parameter(description = "OAuth2 client ID") @RequestParam(required = false) String client_id,
            @Parameter(description = "OAuth2 redirect URI") @RequestParam(required = false) String redirect_uri) {
        return mobileIdService.startMobileId(personalCode, phoneNumber, countryCode, client_id, redirect_uri);
    }

    @GetMapping("/mobileid/check")
    @Operation(summary = "Check Mobile-ID authentication status", description = "Polls the status of an ongoing Mobile-ID authentication session")
    public MobileIdCheckResponse checkMobileId(
            @Parameter(description = "Mobile-ID session identifier", required = true) @RequestParam String sessionId,
            @Parameter(description = "OAuth2 client ID") @RequestParam(required = false) String client_id,
            @Parameter(description = "OAuth2 redirect URI") @RequestParam(required = false) String redirect_uri,
            @Parameter(description = "OAuth2 response type") @RequestParam(required = false) String response_type,
            @Parameter(description = "OAuth2 scope") @RequestParam(required = false) String scope,
            @Parameter(description = "OAuth2 state parameter") @RequestParam(required = false) String state,
            @Parameter(description = "OIDC nonce parameter") @RequestParam(required = false) String nonce) {
        return mobileIdService.checkMobileId(sessionId, client_id, redirect_uri, response_type, scope, state, nonce);
    }
}
