package com.carrental.service;

public class OAuthProfile {
    private final String provider;
    private final String subject;
    private final String email;
    private final String fullName;
    private final boolean emailVerified;

    public OAuthProfile(
            String provider,
            String subject,
            String email,
            String fullName,
            boolean emailVerified) {
        this.provider = provider;
        this.subject = subject;
        this.email = email;
        this.fullName = fullName;
        this.emailVerified = emailVerified;
    }

    public String getProvider() {
        return provider;
    }

    public String getSubject() {
        return subject;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }
}
