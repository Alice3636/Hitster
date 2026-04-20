package com.hitster.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlayerScore {

    private int rank;

    @JsonProperty("id")
    private int userId;

    private String player;
    private int winnings;

    public PlayerScore() {
    }

    public PlayerScore(int rank, int userId, String player, int winnings) {
        this.rank = rank;
        this.userId = userId;
        this.player = player;
        this.winnings = winnings;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public int getWinnings() {
        return winnings;
    }

    public void setWinnings(int winnings) {
        this.winnings = winnings;
    }
}