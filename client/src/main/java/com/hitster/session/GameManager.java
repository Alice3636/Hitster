package com.hitster.session;

import com.hitster.dto.game.CardDTO;
import com.hitster.dto.game.GamePhase;
import com.hitster.dto.game.GameStateDTO;
import com.hitster.dto.game.PlayerGameStateDTO;

import java.util.List;

/**
 * Maintains the current game id and latest game state for the active client session.
 */
public class GameManager {

    private static GameManager instance;

    private String currentGameId;
    private GameStateDTO currentGameState;

    private GameManager() {}

    /**
     * Returns the singleton game manager shared by game-related controllers.
     *
     * @return current game manager instance
     */
    public static synchronized GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    /**
     * Starts tracking a new game and clears any stale game state.
     *
     * @param newGameId server id of the game to track
     */
    public void startGame(String newGameId) {
        this.currentGameId = newGameId;
        this.currentGameState = null;
    }

    /**
     * Replaces the cached game state with the latest server snapshot.
     *
     * @param newState latest game state returned by the server
     */
    public void updateGameState(GameStateDTO newState) {
        this.currentGameState = newState;
    }

    /**
     * Returns the active game id.
     *
     * @return current game id, or {@code null} when no game is active
     */
    public String getGameId() {
        return currentGameId;
    }

    /**
     * Returns the most recent game state snapshot.
     *
     * @return cached game state, or {@code null} before the first state load
     */
    public GameStateDTO getCurrentGameState() {
        return currentGameState;
    }

    /**
     * Indicates whether a game state snapshot is currently available.
     *
     * @return {@code true} when the manager has a cached game state
     */
    public boolean hasGameState() {
        return currentGameState != null;
    }

    /**
     * Returns the current game phase from the cached state.
     *
     * @return current phase, or {@code null} when no state is cached
     */
    public GamePhase getPhase() {
        return currentGameState != null ? currentGameState.phase() : null;
    }

    /**
     * Checks whether the logged-in user is the active player for the current turn.
     *
     * @return {@code true} when the active player id matches the session user id
     */
    public boolean isMyTurn() {
        if (currentGameState == null) return false;

        Long myId = UserSession.getInstance().getUserId();
        Long activePlayerId = currentGameState.activePlayerId();

        return myId != null && activePlayerId != null && myId.equals(activePlayerId);
    }

    /**
     * Resolves the logged-in player's game state from the cached player list.
     *
     * @return current user's player state, or {@code null} when unavailable
     */
    public PlayerGameStateDTO getMe() {
        if (currentGameState == null || currentGameState.players() == null) {
            return null;
        }

        Long myId = UserSession.getInstance().getUserId();
        if (myId == null) return null;

        return currentGameState.players()
                .stream()
                .filter(player -> myId.equals(player.playerId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Resolves the opponent's game state from the cached player list.
     *
     * @return opponent player state, or {@code null} when unavailable
     */
    public PlayerGameStateDTO getOpponent() {
        if (currentGameState == null || currentGameState.players() == null) {
            return null;
        }

        Long myId = UserSession.getInstance().getUserId();
        if (myId == null) return null;

        return currentGameState.players()
                .stream()
                .filter(player -> !myId.equals(player.playerId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the logged-in player's timeline cards.
     *
     * @return current user's timeline, or {@code null} when unavailable
     */
    public List<CardDTO> getMyTimeline() {
        PlayerGameStateDTO me = getMe();
        return me != null ? me.timeline() : null;
    }

    /**
     * Returns the opponent's timeline cards.
     *
     * @return opponent timeline, or {@code null} when unavailable
     */
    public List<CardDTO> getOpponentTimeline() {
        PlayerGameStateDTO opponent = getOpponent();
        return opponent != null ? opponent.timeline() : null;
    }

    /**
     * Returns the logged-in player's available challenge tokens.
     *
     * @return token count, or {@code 0} when no player state is cached
     */
    public int getMyTokens() {
        PlayerGameStateDTO me = getMe();
        return me != null ? me.tokens() : 0;
    }

    /**
     * Indicates whether the cached game has reached the finished phase.
     *
     * @return {@code true} when the game is finished
     */
    public boolean isGameFinished() {
        return currentGameState != null && currentGameState.phase() == GamePhase.FINISHED;
    }

    /**
     * Clears the active game id and cached state after a match ends.
     */
    public void endGame() {
        this.currentGameId = null;
        this.currentGameState = null;
    }
}
