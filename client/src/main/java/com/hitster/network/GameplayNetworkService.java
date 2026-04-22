package com.hitster.network;

import com.google.gson.Gson;
import com.hitster.config.AppConfig;
import com.hitster.session.UserSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GameplayNetworkService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public CompletableFuture<HttpResponse<String>> placeSong(String gameId, int index, Long songId) {
        String payload = gson.toJson(Map.of("index_position", index, "songId", songId));
        return sendPost("/games/" + gameId + "/place", payload);
    }

    public CompletableFuture<HttpResponse<String>> submitGuess(String gameId, String artist, String songName) {
        String payload = gson.toJson(Map.of("artist", artist, "song_name", songName));
        return sendPost("/games/" + gameId + "/guess", payload);
    }

    public CompletableFuture<HttpResponse<String>> getGameState(String gameId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/games/" + gameId + "/state"))
                .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> challenge(String gameId, int index) {
        String payload = gson.toJson(Map.of("suggested_index", index));
        return sendPost("/games/" + gameId + "/challenge", payload);
    }

    // הפונקציה החדשה שמדלגת על חלון האתגר!
    public CompletableFuture<HttpResponse<String>> skipChallenge(String gameId) {
        return sendPost("/games/" + gameId + "/challenge/skip", "");
    }

    public CompletableFuture<HttpResponse<String>> quitGame(String gameId) {
        return sendPost("/games/" + gameId + "/quit", "");
    }

    private CompletableFuture<HttpResponse<String>> sendPost(String endpoint, String jsonPayload) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + endpoint))
                .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
}