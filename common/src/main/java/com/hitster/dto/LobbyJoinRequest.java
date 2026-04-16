package com.hitster.dto;

public class LobbyJoinRequest {
    private int playerId;
    private String username;

    public LobbyJoinRequest() {
    }

    public LobbyJoinRequest(int playerId, String username) {
        this.playerId = playerId;
        this.username = username;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getUsername() {
        return username;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}