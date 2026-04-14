package com.hitster.network;

import com.hitster.config.AppConfig;
import com.hitster.session.UserSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class UserNetworkService {
    
    private final HttpClient httpClient;

    public UserNetworkService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public CompletableFuture<HttpResponse<String>> getLeaderboard() {
        String token = UserSession.getInstance().getToken();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/users/leaderboard"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> getUserProfile() {
        String token = UserSession.getInstance().getToken();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/users/me"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> deleteAccount() {
        String token = UserSession.getInstance().getToken();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/users/me"))
                .header("Authorization", "Bearer " + token)
                .DELETE()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
}