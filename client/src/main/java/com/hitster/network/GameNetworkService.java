package com.hitster.network;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Provides client-side access to lobby matchmaking endpoints.
 */
public class GameNetworkService {

    private final ApiClient apiClient;

    /**
     * Creates a lobby network service backed by the shared API client.
     */
    public GameNetworkService() {
        this.apiClient = new ApiClient();
    }

    /**
     * Adds the current user to the matchmaking queue.
     *
     * @return asynchronous HTTP response for the join request
     */
    public CompletableFuture<HttpResponse<String>> joinLobby() {
        return apiClient.post("/lobby/join");
    }

    /**
     * Checks whether the current matchmaking request has found an opponent.
     *
     * @return asynchronous HTTP response containing the lobby status
     */
    public CompletableFuture<HttpResponse<String>> checkMatchStatus() {
        return apiClient.get("/lobby/status");
    }

    /**
     * Removes the current user from the matchmaking queue.
     *
     * @return asynchronous HTTP response for the leave request
     */
    public CompletableFuture<HttpResponse<String>> leaveLobby() {
        return apiClient.post("/lobby/leave");
    }
}
