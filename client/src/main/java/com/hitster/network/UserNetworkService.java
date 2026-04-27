package com.hitster.network;

import com.hitster.dto.user.UpdateProfileRequestDTO;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Provides client-side access to user profile, leaderboard, and account endpoints.
 */
public class UserNetworkService {

    private final ApiClient apiClient;

    /**
     * Creates a user network service backed by the shared API client.
     */
    public UserNetworkService() {
        this.apiClient = new ApiClient();
    }

    /**
     * Fetches leaderboard entries for display in the client.
     *
     * @return asynchronous HTTP response containing leaderboard data
     */
    public CompletableFuture<HttpResponse<String>> getLeaderboard() {
        return apiClient.get("/users/leaderboard");
    }

    /**
     * Fetches the logged-in user's profile details.
     *
     * @return asynchronous HTTP response containing profile data
     */
    public CompletableFuture<HttpResponse<String>> getUserProfile() {
        return apiClient.get("/users/me");
    }

    /**
     * Requests deletion of the logged-in user's account.
     *
     * @return asynchronous HTTP response for the delete request
     */
    public CompletableFuture<HttpResponse<String>> deleteAccount() {
        return apiClient.delete("/users/me");
    }

    /**
     * Updates editable profile fields for the logged-in user.
     *
     * @param newUsername replacement username
     * @param newEmail replacement email address
     * @return asynchronous HTTP response for the profile update
     */
    public CompletableFuture<HttpResponse<String>> updateProfileDetails(String newUsername, String newEmail) {
        UpdateProfileRequestDTO requestDTO = new UpdateProfileRequestDTO(newUsername, newEmail, null);
        return apiClient.put("/users/me", requestDTO);
    }
}
