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

    public CompletableFuture<HttpResponse<String>> placeSong(Long gameId, int index, Long songId) {
        String payload = gson.toJson(Map.of("index_position", index, "songId", songId));
        return sendPost("/games/" + gameId + "/place", payload);
    }

    public CompletableFuture<HttpResponse<String>> submitGuess(Long gameId, String artist, String songName) {
        String payload = gson.toJson(Map.of("artist", artist, "song_name", songName));
        return sendPost("/games/" + gameId + "/guess", payload);
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