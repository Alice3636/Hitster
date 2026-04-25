package com.hitster.network;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class GameNetworkService {

    private final ApiClient apiClient;

    public GameNetworkService() {
        this.apiClient = new ApiClient();
    }

    public CompletableFuture<HttpResponse<String>> joinLobby() {
        return apiClient.post("/lobby/join");
    }

    public CompletableFuture<HttpResponse<String>> checkMatchStatus() {
        return apiClient.get("/lobby/status");
    }

    public CompletableFuture<HttpResponse<String>> leaveLobby() {
        return apiClient.post("/lobby/leave");
    }
}
