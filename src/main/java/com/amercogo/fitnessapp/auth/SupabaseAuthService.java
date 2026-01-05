package com.amercogo.fitnessapp.auth;

import com.amercogo.fitnessapp.util.Config;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SupabaseAuthService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private final String baseUrl;
    private final String anonKey;

    public SupabaseAuthService() {
        this.baseUrl = Config.get("supabase.url");
        this.anonKey = Config.get("supabase.anonKey");
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("Missing supabase.url in config.properties");
        }
        if (anonKey == null || anonKey.isBlank()) {
            throw new IllegalStateException("Missing supabase.anonKey in config.properties");
        }
    }

    public void signUp(String email, String password) throws Exception {
        String url = baseUrl + "/auth/v1/signup";

        String body = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(escape(email), escape(password));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", anonKey)
                .header("Authorization", "Bearer " + anonKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> res = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 200 && res.statusCode() < 300) {
            // ako je email confirmation ON, Supabase neće vratiti access_token odmah - to je OK
            return;
        }

        String msg = extractError(res.body());
        throw new RuntimeException("Sign up failed (" + res.statusCode() + "): " + msg);
    }

    public void signInWithPassword(String email, String password) throws Exception {
        String url = baseUrl + "/auth/v1/token?grant_type=password";

        String body = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(escape(email), escape(password));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", anonKey)
                .header("Authorization", "Bearer " + anonKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> res = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 200 && res.statusCode() < 300) {
            JsonNode root = MAPPER.readTree(res.body());
            String accessToken = root.path("access_token").asText(null);
            String refreshToken = root.path("refresh_token").asText(null);
            String userId = root.path("user").path("id").asText(null);
            String mail = root.path("user").path("email").asText(null);

            if (accessToken == null || accessToken.isBlank()) {
                throw new RuntimeException("Login failed: missing access_token");
            }

            SessionManager.setSession(accessToken, refreshToken, userId, mail);
            return;
        }

        String msg = extractError(res.body());
        throw new RuntimeException("Login failed (" + res.statusCode() + "): " + msg);
    }

    public void signOut() {
        // za desktop je dovoljno local logout (brisanje tokena)
        SessionManager.clear();
    }

    private static String extractError(String body) {
        try {
            JsonNode n = MAPPER.readTree(body);
            if (n.has("msg")) return n.get("msg").asText();
            if (n.has("error_description")) return n.get("error_description").asText();
            if (n.has("error")) return n.get("error").asText();
        } catch (Exception ignored) { }
        return body == null ? "Unknown error" : body;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
