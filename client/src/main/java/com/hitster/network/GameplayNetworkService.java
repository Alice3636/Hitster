package com.hitster.network;

import com.hitster.dto.game.ChallengeRequestDTO;
import com.hitster.dto.game.GuessSongRequestDTO;
import com.hitster.dto.game.PlaceSongRequestDTO;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Provides client-side access to active game state and gameplay action endpoints.
 */
public class GameplayNetworkService {

    private final ApiClient apiClient = new ApiClient();

    /**
     * Fetches the latest state snapshot for a game.
     *
     * @param gameId server id of the game
     * @return asynchronous HTTP response containing the game state
     */
    public CompletableFuture<HttpResponse<String>> getGameState(String gameId) {
        return apiClient.get("/games/" + gameId + "/state");
    }

    /**
     * Submits the player's artist and title guess for the current song.
     *
     * @param gameId server id of the game
     * @param artist guessed artist name
     * @param title guessed song title
     * @return asynchronous HTTP response for the guess submission
     */
    public CompletableFuture<HttpResponse<String>> submitGuess(String gameId, String artist, String title) {
        return apiClient.post("/games/" + gameId + "/guess", new GuessSongRequestDTO(artist, title));
    }

    /**
     * Places the current song into the selected timeline position.
     *
     * @param gameId server id of the game
     * @param indexPosition target timeline insertion index
     * @param songId identifier of the song being placed
     * @return asynchronous HTTP response for the placement request
     */
    public CompletableFuture<HttpResponse<String>> placeSong(String gameId, int indexPosition, Long songId) {
        return apiClient.post("/games/" + gameId + "/place", new PlaceSongRequestDTO(indexPosition, songId));
    }

    /**
     * Submits a challenge placement against the active player's chosen position.
     *
     * @param gameId server id of the game
     * @param suggestedIndex timeline index suggested by the challenger
     * @return asynchronous HTTP response for the challenge request
     */
    public CompletableFuture<HttpResponse<String>> challenge(String gameId, int suggestedIndex) {
        return apiClient.post("/games/" + gameId + "/challenge", new ChallengeRequestDTO(suggestedIndex));
    }

    /**
     * Skips the current challenge opportunity for the logged-in player.
     *
     * @param gameId server id of the game
     * @return asynchronous HTTP response for the skip request
     */
    public CompletableFuture<HttpResponse<String>> skipChallenge(String gameId) {
        return apiClient.post("/games/" + gameId + "/challenge/skip");
    }

    /**
     * Quits the active game on behalf of the logged-in player.
     *
     * @param gameId server id of the game
     * @return asynchronous HTTP response for the quit request
     */
    public CompletableFuture<HttpResponse<String>> quitGame(String gameId) {
        return apiClient.post("/games/" + gameId + "/quit");
    }
}
