package com.hitster.dto;

public class LobbyJoinResponse {
    private int roomId;
    private boolean roomFull;
    private boolean gameStarted;
    private int gameId;
    private String message;

    public LobbyJoinResponse() {
    }

    public LobbyJoinResponse(int roomId, boolean roomFull, boolean gameStarted, int gameId, String message) {
        this.roomId = roomId;
        this.roomFull = roomFull;
        this.gameStarted = gameStarted;
        this.gameId = gameId;
        this.message = message;
    }

    public int getRoomId() {
        return roomId;
    }

    public boolean isRoomFull() {
        return roomFull;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public int getGameId() {
        return gameId;
    }

    public String getMessage() {
        return message;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public void setRoomFull(boolean roomFull) {
        this.roomFull = roomFull;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}