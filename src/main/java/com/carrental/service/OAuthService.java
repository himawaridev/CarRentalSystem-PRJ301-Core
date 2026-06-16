package com.carrental.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public class OAuthService {
    public static final String GOOGLE = "google";
    public static final String FACEBOOK = "facebook";

    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USERINFO_URL = "https://openidconnect.googleapis.com/v1/userinfo";
    private static final String FACEBOOK_AUTH_URL = "https://www.facebook.com/";
    private static final String FACEBOOK_GRAPH_URL = "https://graph.facebook.com/";

    private final AuthConfig config;
    private final HttpClient httpClient;

    public OAuthService() {
        this(new AuthConfig());
    }

    public OAuthService(AuthConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder().build();
    }

    public boolean isConfigured(String provider) {
        return config.providerConfigured(provider);
    }

    public String buildAuthorizationUrl(String provider, String state) {
        if (GOOGLE.equals(provider)) {
            return GOOGLE_AUTH_URL
                    + "?client_id=" + encode(config.googleClientId())
                    + "&redirect_uri=" + encode(config.redirectUri(provider))
                    + "&response_type=code"
                    + "&scope=" + encode("openid email profile")
                    + "&state=" + encode(state)
                    + "&prompt=select_account";
        }
        if (FACEBOOK.equals(provider)) {
            String version = config.facebookGraphVersion();
            return FACEBOOK_AUTH_URL + version + "/dialog/oauth"
                    + "?client_id=" + encode(config.facebookClientId())
                    + "&redirect_uri=" + encode(config.redirectUri(provider))
                    + "&response_type=code"
                    + "&scope=" + encode("email,public_profile")
                    + "&state=" + encode(state);
        }
        throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
    }

    public OAuthProfile fetchProfile(String provider, String code) throws IOException, InterruptedException {
        String accessToken = exchangeCodeForAccessToken(provider, code);
        if (GOOGLE.equals(provider)) {
            return fetchGoogleProfile(accessToken);
        }
        if (FACEBOOK.equals(provider)) {
            return fetchFacebookProfile(accessToken);
        }
        throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
    }

    private String exchangeCodeForAccessToken(String provider, String code)
            throws IOException, InterruptedException {
        String tokenUrl;
        String body;
        if (GOOGLE.equals(provider)) {
            tokenUrl = GOOGLE_TOKEN_URL;
            body = form(
                    "code", code,
                    "client_id", config.googleClientId(),
                    "client_secret", config.googleClientSecret(),
                    "redirect_uri", config.redirectUri(provider),
                    "grant_type", "authorization_code");
        } else if (FACEBOOK.equals(provider)) {
            tokenUrl = FACEBOOK_GRAPH_URL + config.facebookGraphVersion() + "/oauth/access_token";
            body = form(
                    "code", code,
                    "client_id", config.facebookClientId(),
                    "client_secret", config.facebookClientSecret(),
                    "redirect_uri", config.redirectUri(provider));
        } else {
            throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());
        if (response.statusCode() < 200 || response.statusCode() >= 300 || !json.has("access_token")) {
            throw new IOException("OAuth token exchange failed: " + response.body());
        }
        return json.getString("access_token");
    }

    private OAuthProfile fetchGoogleProfile(String accessToken) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GOOGLE_USERINFO_URL))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Google userinfo failed: " + response.body());
        }
        return new OAuthProfile(
                GOOGLE,
                json.optString("sub", ""),
                json.optString("email", ""),
                json.optString("name", ""),
                json.optBoolean("email_verified", false));
    }

    private OAuthProfile fetchFacebookProfile(String accessToken) throws IOException, InterruptedException {
        String url = FACEBOOK_GRAPH_URL + config.facebookGraphVersion()
                + "/me?fields=id,name,email&access_token=" + encode(accessToken);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Facebook profile failed: " + response.body());
        }
        return new OAuthProfile(
                FACEBOOK,
                json.optString("id", ""),
                json.optString("email", ""),
                json.optString("name", ""),
                !json.optString("email", "").isBlank());
    }

    private static String form(String... pairs) {
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < pairs.length; i += 2) {
            if (i > 0) {
                body.append('&');
            }
            body.append(encode(pairs[i])).append('=').append(encode(pairs[i + 1]));
        }
        return body.toString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
