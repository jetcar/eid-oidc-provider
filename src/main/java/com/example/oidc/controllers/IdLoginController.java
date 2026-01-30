package com.example.oidc.controllers;

import com.example.oidc.dto.IdCardChallengeResponse;
import com.example.oidc.dto.IdCardLoginRequest;
import com.example.oidc.dto.IdCardLoginResponse;
import com.example.oidc.service.IIdcardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import java.security.cert.CertificateEncodingException;

@RestController
@RequestMapping("/idlogin")
@Tag(name = "ID-Card Authentication", description = "Endpoints for Estonian ID-Card authentication flow")
public class IdLoginController {

    private final IIdcardService idcardService;

    @Autowired
    public IdLoginController(IIdcardService idcardService) {
        this.idcardService = idcardService;
    }

    @GetMapping("/challenge")
    @Operation(summary = "Get authentication challenge", description = "Generates a challenge nonce for ID-Card authentication")
    public IdCardChallengeResponse challenge(
            @Parameter(description = "OAuth2 client ID") @RequestParam(value = "client_id", required = false) String clientId,
            @Parameter(description = "OAuth2 redirect URI") @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @Parameter(description = "OAuth2 state parameter") @RequestParam(value = "state", required = false) String state,
            @Parameter(description = "OIDC nonce parameter") @RequestParam(value = "nonce", required = false) String nonce,
            @Parameter(description = "PKCE code challenge") @RequestParam(value = "code_challenge", required = false) String codeChallenge,
            @Parameter(description = "PKCE code challenge method (S256 or plain)") @RequestParam(value = "code_challenge_method", required = false) String codeChallengeMethod) {
        return idcardService.createChallenge(clientId, redirectUri, state, nonce, codeChallenge, codeChallengeMethod);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Login with ID-Card", description = "Authenticates user with ID-Card certificate and signature")
    public IdCardLoginResponse login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "ID-Card authentication data including certificate and signature") @RequestBody IdCardLoginRequest body,
            @Parameter(description = "OAuth2 client ID") @RequestParam(value = "client_id", required = false) String clientId,
            @Parameter(description = "OAuth2 redirect URI") @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @Parameter(description = "OAuth2 state parameter") @RequestParam(value = "state", required = false) String state,
            @Parameter(description = "OIDC nonce parameter") @RequestParam(value = "nonce", required = false) String nonce,
            @Parameter(description = "Challenge session identifier") @RequestParam(value = "sessionId", required = false) String sessionId)
            throws CertificateEncodingException {
        return idcardService.login(body, clientId, redirectUri, state, nonce, sessionId);
    }
}
