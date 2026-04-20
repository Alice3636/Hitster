package com.hitster.model;

public class MatchHistoryObj {
     private String enemy_username;
     private String date;
     private String gameStatus;

     public MatchHistoryObj (String enemy_username, String date, String gameStatus) {
          this.enemy_username = enemy_username;
          this.date = date;
          this.gameStatus = gameStatus;
     }

     public String getEnemy_username () {
          return enemy_username;
     }

     public String getDate () {
          return date;
     }

     public String getGameStatus () {
          return gameStatus;
     }
}