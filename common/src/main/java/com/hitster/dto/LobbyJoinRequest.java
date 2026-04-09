package com.hitster.dto;

public class LobbyJoinRequest {
    private String playerId;
    private String username;

    public LobbyJoinRequest() {
    }

    public LobbyJoinRequest(String playerId, String username) {
        this.playerId = playerId;
        this.username = username;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getUsername() {
        return username;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}