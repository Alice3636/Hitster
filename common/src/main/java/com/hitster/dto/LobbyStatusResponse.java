package com.hitster.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LobbyStatusResponse {

    private String status;

    @JsonProperty("game_id")
    private String gameId;

    public LobbyStatusResponse() {
    }

    public LobbyStatusResponse(String status, String gameId) {
        this.status = status;
        this.gameId = gameId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
}