package com.hitster.dto;

public class MatchHistoryDTO {
    
    private String enemyUsername; 
    private String date;
    private String gameStatus;

    public MatchHistoryDTO(String enemyUsername, String date, String gameStatus) {
         this.enemyUsername = enemyUsername;
         this.date = date;
         this.gameStatus = gameStatus;
    }

    public String getEnemyUsername() {
         return enemyUsername;
    }

    public String getDate() {
         return date;
    }

    public String getGameStatus() {
         return gameStatus;
    }
    
    public void setEnemyUsername(String enemyUsername) {
        this.enemyUsername = enemyUsername;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setGameStatus(String gameStatus) {
        this.gameStatus = gameStatus;
    }
}