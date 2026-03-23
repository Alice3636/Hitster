package com.hitster.model;

public class PlayerScore {
    private int rank; 
    private int id;
    private String player;
    private int winnings;

    public PlayerScore(int rank, int id, String player, int winnings) {
        this.rank = rank;
        this.id = id;
        this.player = player;
        this.winnings = winnings;
    }

    public int getRank() { return rank; }
    public int getId() { return id; }
    public String getPlayer() { return player; }
    public int getWinnings() { return winnings; } 
}