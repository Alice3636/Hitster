package com.hitster.network;

import com.hitster.config.AppConfig;
import com.hitster.session.UserSession;
import com.fasterxml.jackson.databind.ObjectMapper; 

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List; 
import java.util.concurrent.CompletableFuture;

public class AdminNetworkService {
    private final HttpClient httpClient;
    private final ObjectMapper mapper; 

    public AdminNetworkService() {
        this.httpClient = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper(); 
    }

    

    public CompletableFuture<HttpResponse<String>> getAllUsers() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/admin/users"))
                .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> deleteUsers(List<Integer> userIds) {
        try {
            String jsonPayload = mapper.writeValueAsString(userIds);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.BASE_API_URL + "/admin/users"))
                    .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                    .header("Content-Type", "application/json")
                    .method("DELETE", HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    

    public CompletableFuture<HttpResponse<String>> getAllSongs() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/admin/songs"))
                .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> deleteSongs(List<Integer> songIds) {
        try {
            String jsonPayload = mapper.writeValueAsString(songIds);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.BASE_API_URL + "/admin/songs"))
                    .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                    .header("Content-Type", "application/json")
                    .method("DELETE", HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}