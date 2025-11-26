package com.example.oidc.service;

import com.example.oidc.dto.IdCardChallengeResponse;
import com.example.oidc.dto.IdCardLoginRequest;
import com.example.oidc.dto.IdCardLoginResponse;

/**
 * Interface for ID-Card authentication service.
 * Handles challenge generation and login validation for Estonian ID-Card
 * authentication.
 */
public interface IIdcardService {

    /**
     * Creates a challenge nonce for ID-Card authentication.
     *
     * @param clientId    the OAuth2 client identifier
     * @param redirectUri the OAuth2 redirect URI
     * @param state       the OAuth2 state parameter
     * @param nonce       the OIDC nonce parameter
     * @return challenge response containing the nonce and session ID
     */
    IdCardChallengeResponse createChallenge(
            String clientId,
            String redirectUri,
            String state,
            String nonce);

    /**
     * Validates ID-Card authentication token and completes login.
     *
     * @param body        the authentication request containing certificate and
     *                    signature
     * @param clientId    the OAuth2 client identifier
     * @param redirectUri the OAuth2 redirect URI
     * @param state       the OAuth2 state parameter
     * @param nonce       the OIDC nonce parameter
     * @param sessionId   the challenge session identifier
     * @return login response with authorization code or error
     */
    IdCardLoginResponse login(
            IdCardLoginRequest body,
            String clientId,
            String redirectUri,
            String state,
            String nonce,
            String sessionId);
}
