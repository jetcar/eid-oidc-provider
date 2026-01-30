package com.example.oidc.dto;

/**
 * Stores PKCE (Proof Key for Code Exchange) parameters for an authorization
 * request.
 * Used to prevent authorization code interception attacks.
 */
public class PkceData {
    private String codeChallenge;
    private String codeChallengeMethod; // "S256" or "plain"

    public PkceData() {
    }

    public PkceData(String codeChallenge, String codeChallengeMethod) {
        this.codeChallenge = codeChallenge;
        this.codeChallengeMethod = codeChallengeMethod;
    }

    public String getCodeChallenge() {
        return codeChallenge;
    }

    public void setCodeChallenge(String codeChallenge) {
        this.codeChallenge = codeChallenge;
    }

    public String getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    public void setCodeChallengeMethod(String codeChallengeMethod) {
        this.codeChallengeMethod = codeChallengeMethod;
    }
}
