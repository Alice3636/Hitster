package com.hitster.network;

import com.google.gson.Gson;
import com.hitster.config.AppConfig;
import com.hitster.dto.game.ChallengeRequestDTO;
import com.hitster.dto.game.GuessSongRequestDTO;
import com.hitster.dto.game.PlaceSongRequestDTO;
import com.hitster.session.UserSession;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class GameplayNetworkService {

    private final HttpClient httpClient;
    private final Gson gson;

    public GameplayNetworkService() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public CompletableFuture<HttpResponse<String>> getGameState(String gameId) {
        String token = UserSession.getInstance().getToken();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/games/" + gameId + "/state"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> submitGuess(String gameId, String artist, String title) {
        String token = UserSession.getInstance().getToken();
        GuessSongRequestDTO requestDTO = new GuessSongRequestDTO(artist, title);
        String payload = gson.toJson(requestDTO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/games/" + gameId + "/guess"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> placeSong(String gameId, int indexPosition, Long songId) {
        String token = UserSession.getInstance().getToken();
        PlaceSongRequestDTO requestDTO = new PlaceSongRequestDTO(indexPosition, songId);
        String payload = gson.toJson(requestDTO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/games/" + gameId + "/place"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> challenge(String gameId, int suggestedIndex) {
        String token = UserSession.getInstance().getToken();
        ChallengeRequestDTO requestDTO = new ChallengeRequestDTO(suggestedIndex);
        String payload = gson.toJson(requestDTO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/games/" + gameId + "/challenge"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> skipChallenge(String gameId) {
        String token = UserSession.getInstance().getToken();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/games/" + gameId + "/challenge/skip"))
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> quitGame(String gameId) {
        String token = UserSession.getInstance().getToken();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/games/" + gameId + "/quit"))
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
}
