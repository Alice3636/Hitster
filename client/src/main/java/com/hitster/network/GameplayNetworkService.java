package com.hitster.network;

import com.hitster.dto.game.ChallengeRequestDTO;
import com.hitster.dto.game.GuessSongRequestDTO;
import com.hitster.dto.game.PlaceSongRequestDTO;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class GameplayNetworkService {

    private final ApiClient apiClient = new ApiClient();

    public CompletableFuture<HttpResponse<String>> getGameState(String gameId) {
        return apiClient.get("/games/" + gameId + "/state");
    }

    public CompletableFuture<HttpResponse<String>> submitGuess(String gameId, String artist, String title) {
        return apiClient.post("/games/" + gameId + "/guess", new GuessSongRequestDTO(artist, title));
    }

    public CompletableFuture<HttpResponse<String>> placeSong(String gameId, int indexPosition, Long songId) {
        return apiClient.post("/games/" + gameId + "/place", new PlaceSongRequestDTO(indexPosition, songId));
    }

    public CompletableFuture<HttpResponse<String>> challenge(String gameId, int suggestedIndex) {
        return apiClient.post("/games/" + gameId + "/challenge", new ChallengeRequestDTO(suggestedIndex));
    }

    public CompletableFuture<HttpResponse<String>> skipChallenge(String gameId) {
        return apiClient.post("/games/" + gameId + "/challenge/skip");
    }

    public CompletableFuture<HttpResponse<String>> quitGame(String gameId) {
        return apiClient.post("/games/" + gameId + "/quit");
    }
}
