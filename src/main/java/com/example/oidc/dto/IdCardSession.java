package com.example.oidc.dto;

public class IdCardSession {
    private boolean complete;
    private String challengeNonce;
    private PkceData pkceData;

    public IdCardSession() {
        // Default constructor
    }

    public IdCardSession(boolean complete, String challengeNonce) {
        this.complete = complete;
        this.challengeNonce = challengeNonce;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public String getChallengeNonce() {
        return challengeNonce;
    }

    public void setChallengeNonce(String challengeNonce) {
        this.challengeNonce = challengeNonce;
    }

    public PkceData getPkceData() {
        return pkceData;
    }

    public void setPkceData(PkceData pkceData) {
        this.pkceData = pkceData;
    }
}
