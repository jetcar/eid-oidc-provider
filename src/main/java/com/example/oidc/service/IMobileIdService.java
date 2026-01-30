package com.example.oidc.service;

import com.example.oidc.dto.MobileIdCheckResponse;
import com.example.oidc.dto.MobileIdStartResponse;

/**
 * Interface for Mobile-ID authentication service.
 * Handles Mobile-ID authentication flow for Estonian, Latvian, and Lithuanian
 * users.
 */
public interface IMobileIdService {

        /**
         * Initiates a Mobile-ID authentication session.
         *
         * @param personalCode        the user's personal identification code
         * @param phoneNumber         the user's phone number
         * @param countryCode         the country code (e.g., +372 for Estonia)
         * @param clientId            the OAuth2 client identifier
         * @param redirectUri         the OAuth2 redirect URI
         * @param codeChallenge       the PKCE code challenge
         * @param codeChallengeMethod the PKCE code challenge method (S256 or plain)
         * @return start response containing session ID and verification code
         */
        MobileIdStartResponse startMobileId(
                        String personalCode,
                        String phoneNumber,
                        String countryCode,
                        String clientId,
                        String redirectUri,
                        String codeChallenge,
                        String codeChallengeMethod);

        /**
         * Checks the status of an ongoing Mobile-ID authentication session.
         *
         * @param sessionId    the Mobile-ID session identifier
         * @param clientId     the OAuth2 client identifier
         * @param redirectUri  the OAuth2 redirect URI
         * @param responseType the OAuth2 response type
         * @param scope        the OAuth2 scope
         * @param state        the OAuth2 state parameter
         * @param nonce        the OIDC nonce parameter
         * @return check response with authentication status and authorization code if
         *         complete
         */
        MobileIdCheckResponse checkMobileId(
                        String sessionId,
                        String clientId,
                        String redirectUri,
                        String responseType,
                        String scope,
                        String state,
                        String nonce);
}
