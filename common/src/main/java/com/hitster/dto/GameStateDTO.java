package com.hitster.dto;
import java.util.List;

public class GameStateDTO {

    private String gameId;
    private String gameStatus;
    private int turnNumber;

    private String currentPlayer;
    private String currentSongTitle;
    private String winnerName;

    private String player1Name;
    private String player2Name;

    private int player1Score;
    private int player2Score;

    private int player1Tokens;
    private int player2Tokens;

    private List<String> player1Timeline;
    private List<String> player2Timeline;

    public GameStateDTO(
            String gameId,
            String gameStatus,
            int turnNumber,
            String currentPlayer,
            String currentSongTitle,
            String winnerName,
            String player1Name,
            String player2Name,
            int player1Score,
            int player2Score,
            int player1Tokens,
            int player2Tokens,
            List<String> player1Timeline,
            List<String> player2Timeline
    ) {
        this.gameId = gameId;
        this.gameStatus = gameStatus;
        this.turnNumber = turnNumber;
        this.currentPlayer = currentPlayer;
        this.currentSongTitle = currentSongTitle;
        this.winnerName = winnerName;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.player1Score = player1Score;
        this.player2Score = player2Score;
        this.player1Tokens = player1Tokens;
        this.player2Tokens = player2Tokens;
        this.player1Timeline = player1Timeline;
        this.player2Timeline = player2Timeline;
    }

    public String getGameId() {
        return gameId;
    }

    public String getGameStatus() {
        return gameStatus;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public String getCurrentSongTitle() {
        return currentSongTitle;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public int getPlayer1Score() {
        return player1Score;
    }

    public int getPlayer2Score() {
        return player2Score;
    }

    public int getPlayer1Tokens() {
        return player1Tokens;
    }

    public int getPlayer2Tokens() {
        return player2Tokens;
    }

    public List<String> getPlayer1Timeline() {
        return player1Timeline;
    }

    public List<String> getPlayer2Timeline() {
        return player2Timeline;
    }
}