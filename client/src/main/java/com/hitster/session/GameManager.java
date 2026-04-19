package com.hitster.session;

import com.hitster.dto.GameStateDTO;

public class GameManager {

    private static GameManager instance;
    private GameStateDTO currentGameState; 

    private GameManager() {}

    public static synchronized GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void startGame(Long newGameId) {
        this.currentGameState = new GameStateDTO();
        this.currentGameState.setGameId(newGameId); 
    }

    public void updateGameState(GameStateDTO newState) {
        this.currentGameState = newState;
    }

    public Long getGameId() {
        return currentGameState != null ? currentGameState.getGameId() : null;
    }

    public boolean isMyTurn() {
        if (currentGameState == null) return false;
        Long myId = UserSession.getInstance().getUserId();
        return myId.equals(currentGameState.getCurrentPlayerId());
    }

    public java.util.List<GameStateDTO.CardDTO> getMyTimeline() {
        if (currentGameState == null) return null;
        Long myId = UserSession.getInstance().getUserId();
        return myId.equals(currentGameState.getPlayer1Id()) ? 
               currentGameState.getPlayer1Timeline() : currentGameState.getPlayer2Timeline();
    }

    public java.util.List<GameStateDTO.CardDTO> getOpponentTimeline() {
        if (currentGameState == null) return null;
        Long myId = UserSession.getInstance().getUserId();
        return myId.equals(currentGameState.getPlayer1Id()) ? 
               currentGameState.getPlayer2Timeline() : currentGameState.getPlayer1Timeline();
    }

    public void endGame() {
        this.currentGameState = null;
    }

    public GameStateDTO getCurrentGameState() {
        return currentGameState;
    }
}
