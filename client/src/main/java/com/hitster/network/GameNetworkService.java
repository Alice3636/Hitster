package com.hitster.network;

import com.hitster.config.AppConfig;
import com.hitster.session.UserSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class GameNetworkService {
    
    private final HttpClient httpClient;

    public GameNetworkService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public CompletableFuture<HttpResponse<String>> joinLobby() {
        String token = UserSession.getInstance().getToken();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/lobby/join"))
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> checkMatchStatus() {
        String token = UserSession.getInstance().getToken();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/lobby/status"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> leaveLobby() {
        String token = UserSession.getInstance().getToken();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/lobby/leave"))
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
}