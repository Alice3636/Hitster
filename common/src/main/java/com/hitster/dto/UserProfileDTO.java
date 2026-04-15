package com.hitster.dto;

import java.util.List;

public class UserProfileDTO {
    private String username;
    private String email;
    private int totalWins;
    private double winRate; 
    private List<MatchHistoryDTO> matchHistory;

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public int getTotalWins() { return totalWins; }
    public double getWinRate() { return winRate; }
    public List<MatchHistoryDTO> getMatchHistory() { return matchHistory; }

    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setTotalWins(int totalWins) { this.totalWins = totalWins; }
    public void setWinRate(double winRate) { this.winRate = winRate; }
    public void setMatchHistory(List<MatchHistoryDTO> matchHistory) { this.matchHistory = matchHistory; }
}