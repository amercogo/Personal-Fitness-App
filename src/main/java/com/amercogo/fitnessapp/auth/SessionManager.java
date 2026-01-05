package com.amercogo.fitnessapp.auth;

public class SessionManager {
    private static String accessToken;
    private static String refreshToken;
    private static String userId;
    private static String email;

    public static boolean isLoggedIn() {
        return accessToken != null && !accessToken.isBlank();
    }

    public static void setSession(String accessToken, String refreshToken, String userId, String email) {
        SessionManager.accessToken = accessToken;
        SessionManager.refreshToken = refreshToken;
        SessionManager.userId = userId;
        SessionManager.email = email;
    }

    public static void clear() {
        accessToken = null;
        refreshToken = null;
        userId = null;
        email = null;
    }

    public static String getAccessToken() { return accessToken; }
    public static String getRefreshToken() { return refreshToken; }
    public static String getUserId() { return userId; }
    public static String getEmail() { return email; }
}
