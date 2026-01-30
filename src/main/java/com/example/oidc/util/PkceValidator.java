package com.example.oidc.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility class for validating PKCE (Proof Key for Code Exchange) code
 * verifiers.
 */
public class PkceValidator {

    /**
     * Validates a code verifier against a code challenge.
     *
     * @param codeVerifier        the code verifier from the client
     * @param codeChallenge       the stored code challenge
     * @param codeChallengeMethod the challenge method ("S256" or "plain")
     * @return true if valid, false otherwise
     */
    public static boolean validate(String codeVerifier, String codeChallenge, String codeChallengeMethod) {
        if (codeVerifier == null || codeChallenge == null) {
            return false;
        }

        if ("plain".equals(codeChallengeMethod)) {
            // For plain method, verifier must match challenge exactly
            return codeVerifier.equals(codeChallenge);
        } else if ("S256".equals(codeChallengeMethod)) {
            // For S256, hash the verifier and compare with challenge
            try {
                String computedChallenge = generateS256Challenge(codeVerifier);
                return computedChallenge.equals(codeChallenge);
            } catch (NoSuchAlgorithmException e) {
                return false;
            }
        }

        // Unknown method
        return false;
    }

    /**
     * Generates an S256 code challenge from a code verifier.
     *
     * @param codeVerifier the code verifier
     * @return the base64url-encoded SHA256 hash
     * @throws NoSuchAlgorithmException if SHA-256 is not available
     */
    private static String generateS256Challenge(String codeVerifier) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }
}
