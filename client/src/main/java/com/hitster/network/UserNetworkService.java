package com.hitster.network;

import com.hitster.dto.user.UpdateProfileRequestDTO;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class UserNetworkService {

    private final ApiClient apiClient;

    public UserNetworkService() {
        this.apiClient = new ApiClient();
    }

    public CompletableFuture<HttpResponse<String>> getLeaderboard() {
        return apiClient.get("/users/leaderboard");
    }

    public CompletableFuture<HttpResponse<String>> getUserProfile() {
        return apiClient.get("/users/me");
    }

    public CompletableFuture<HttpResponse<String>> deleteAccount() {
        return apiClient.delete("/users/me");
    }

    public CompletableFuture<HttpResponse<String>> updateProfileDetails(String newUsername, String newEmail) {
        UpdateProfileRequestDTO requestDTO = new UpdateProfileRequestDTO(newUsername, newEmail, null);
        return apiClient.put("/users/me", requestDTO);
    }
}
