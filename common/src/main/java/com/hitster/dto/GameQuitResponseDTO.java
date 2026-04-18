package com.hitster.dto;

public class GameQuitResponseDTO {

    private String gameId;
    private String gameStatus;
    private String winnerName;
    private String message;

    public GameQuitResponseDTO() {
    }

    public GameQuitResponseDTO(String gameId, String gameStatus, String winnerName, String message) {
        this.gameId = gameId;
        this.gameStatus = gameStatus;
        this.winnerName = winnerName;
        this.message = message;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(String gameStatus) {
        this.gameStatus = gameStatus;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}