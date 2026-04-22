package com.hitster.network;

import com.hitster.config.AppConfig;
import com.hitster.dto.admin.DeleteSongsRequestDTO;
import com.hitster.dto.admin.DeleteUsersRequestDTO;
import com.hitster.session.UserSession;
import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List; 
import java.util.concurrent.CompletableFuture;

public class AdminNetworkService {
    private final HttpClient httpClient;

    public AdminNetworkService() {
        this.httpClient = HttpClient.newHttpClient();
    }
    
    public CompletableFuture<HttpResponse<String>> getAllUsers() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/admin/users"))
                .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> deleteUsers(List<Long> userIds) {
        String token = UserSession.getInstance().getToken();
        DeleteUsersRequestDTO deleteUsersRequest = new DeleteUsersRequestDTO(userIds);

        Gson gson = new Gson();
        String jsonPayload = gson.toJson(deleteUsersRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/admin/users"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .method("DELETE", HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> getAllSongs() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/admin/songs"))
                .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> deleteSongs(List<Long> songIds) {
        
        String token = UserSession.getInstance().getToken();
        DeleteSongsRequestDTO deleteSongsRequest = new DeleteSongsRequestDTO(songIds);

        Gson gson = new Gson();
        String jsonPayload = gson.toJson(deleteSongsRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/admin/songs"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .method("DELETE", HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
}