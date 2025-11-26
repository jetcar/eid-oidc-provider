package com.example.oidc.service;

import com.example.oidc.dto.SmartIdCheckResponse;
import com.example.oidc.dto.SmartIdStartResponse;

/**
 * Interface for Smart-ID authentication service.
 * Handles Smart-ID authentication flow for Estonian, Latvian, and Lithuanian users.
 */
public interface ISmartIdService {

    /**
     * Initiates a Smart-ID authentication session.
     *
     * @param country the user's country (Estonia, Latvia, or Lithuania)
     * @param personalCode the user's personal identification code
     * @return start response containing session ID and verification code
     */
    SmartIdStartResponse startSmartId(
            String country,
            String personalCode);

    /**
     * Checks the status of an ongoing Smart-ID authentication session.
     *
     * @param sessionId the Smart-ID session identifier
     * @param clientId the OAuth2 client identifier
     * @param redirectUri the OAuth2 redirect URI
     * @param responseType the OAuth2 response type
     * @param scope the OAuth2 scope
     * @param state the OAuth2 state parameter
     * @param nonce the OIDC nonce parameter
     * @return check response with authentication status and authorization code if complete
     */
    SmartIdCheckResponse checkSmartId(
            String sessionId,
            String clientId,
            String redirectUri,
            String responseType,
            String scope,
            String state,
            String nonce);
}
