package com.hitster.dto;

public class LobbyJoinResponse {
    private String roomId;
    private boolean roomFull;
    private boolean gameStarted;
    private String gameId;
    private String message;

    public LobbyJoinResponse() {
    }

    public LobbyJoinResponse(String roomId, boolean roomFull, boolean gameStarted, String gameId, String message) {
        this.roomId = roomId;
        this.roomFull = roomFull;
        this.gameStarted = gameStarted;
        this.gameId = gameId;
        this.message = message;
    }

    public String getRoomId() {
        return roomId;
    }

    public boolean isRoomFull() {
        return roomFull;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public String getGameId() {
        return gameId;
    }

    public String getMessage() {
        return message;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setRoomFull(boolean roomFull) {
        this.roomFull = roomFull;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}