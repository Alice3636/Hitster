package com.hitster.session;

import com.hitster.dto.game.CardDTO;
import com.hitster.dto.game.GamePhase;
import com.hitster.dto.game.GameStateDTO;
import com.hitster.dto.game.PlayerGameStateDTO;

import java.util.List;

public class GameManager {

    private static GameManager instance;

    private String currentGameId;
    private GameStateDTO currentGameState;

    private GameManager() {}

    public static synchronized GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void startGame(String newGameId) {
        this.currentGameId = newGameId;
        this.currentGameState = null;
    }

    public void updateGameState(GameStateDTO newState) {
        this.currentGameState = newState;
    }

    public String getGameId() {
        return currentGameId;
    }

    public GameStateDTO getCurrentGameState() {
        return currentGameState;
    }

    public boolean hasGameState() {
        return currentGameState != null;
    }

    public GamePhase getPhase() {
        return currentGameState != null ? currentGameState.phase() : null;
    }

    public boolean isMyTurn() {
        if (currentGameState == null) return false;

        Long myId = UserSession.getInstance().getUserId();
        Long activePlayerId = currentGameState.activePlayerId();

        return myId != null && activePlayerId != null && myId.equals(activePlayerId);
    }

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

    public List<CardDTO> getMyTimeline() {
        PlayerGameStateDTO me = getMe();
        return me != null ? me.timeline() : null;
    }

    public List<CardDTO> getOpponentTimeline() {
        PlayerGameStateDTO opponent = getOpponent();
        return opponent != null ? opponent.timeline() : null;
    }

    public int getMyTokens() {
        PlayerGameStateDTO me = getMe();
        return me != null ? me.tokens() : 0;
    }

    public boolean isGameFinished() {
        return currentGameState != null && currentGameState.phase() == GamePhase.FINISHED;
    }

    public void endGame() {
        this.currentGameId = null;
        this.currentGameState = null;
    }
}
