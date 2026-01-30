package com.example.oidc.dto;

public class SmartIdSession {
    private boolean complete;
    private String country;
    private String personalCode;
    private String authenticationHash;
    private PkceData pkceData;

    public SmartIdSession() {
        // default constructor
    }

    public SmartIdSession(boolean complete, String country, String personalCode, String authenticationHash) {
        this.complete = complete;
        this.country = country;
        this.personalCode = personalCode;
        this.authenticationHash = authenticationHash;
    }

    public boolean isComplete() {
        return complete;
    }

    public String getCountry() {
        return country;
    }

    public String getPersonalCode() {
        return personalCode;
    }

    public String getAuthenticationHash() {
        return authenticationHash;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setPersonalCode(String personalCode) {
        this.personalCode = personalCode;
    }

    public void setAuthenticationHash(String authenticationHash) {
        this.authenticationHash = authenticationHash;
    }

    public PkceData getPkceData() {
        return pkceData;
    }

    public void setPkceData(PkceData pkceData) {
        this.pkceData = pkceData;
    }
}