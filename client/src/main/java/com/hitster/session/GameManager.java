package com.hitster.dto;

import java.util.List;

public class GameStateDTO {

    private String gameId;
    private String gameStatus;
    private int turnNumber;
    private int timeLeftSeconds;

    private Long currentPlayerId;
    private CurrentSongDTO currentSong;

    private String winnerName;

    private Long player1Id;
    private String player1Name;
    private int player1Score;
    private int player1Tokens;
    private List<CardDTO> player1Timeline;

    private Long player2Id;
    private String player2Name;
    private int player2Score;
    private int player2Tokens;
    private List<CardDTO> player2Timeline;

    public GameStateDTO() {
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(String gameStatus) {
        this.gameStatus = gameStatus;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
    }

    public int getTimeLeftSeconds() {
        return timeLeftSeconds;
    }

    public void setTimeLeftSeconds(int timeLeftSeconds) {
        this.timeLeftSeconds = timeLeftSeconds;
    }

    public Long getCurrentPlayerId() {
        return currentPlayerId;
    }

    public void setCurrentPlayerId(Long currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }

    public CurrentSongDTO getCurrentSong() {
        return currentSong;
    }

    public void setCurrentSong(CurrentSongDTO currentSong) {
        this.currentSong = currentSong;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }

    public Long getPlayer1Id() {
        return player1Id;
    }

    public void setPlayer1Id(Long player1Id) {
        this.player1Id = player1Id;
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public void setPlayer1Name(String player1Name) {
        this.player1Name = player1Name;
    }

    public int getPlayer1Score() {
        return player1Score;
    }

    public void setPlayer1Score(int player1Score) {
        this.player1Score = player1Score;
    }

    public int getPlayer1Tokens() {
        return player1Tokens;
    }

    public void setPlayer1Tokens(int player1Tokens) {
        this.player1Tokens = player1Tokens;
    }

    public List<CardDTO> getPlayer1Timeline() {
        return player1Timeline;
    }

    public void setPlayer1Timeline(List<CardDTO> player1Timeline) {
        this.player1Timeline = player1Timeline;
    }

    public Long getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(Long player2Id) {
        this.player2Id = player2Id;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public void setPlayer2Name(String player2Name) {
        this.player2Name = player2Name;
    }

    public int getPlayer2Score() {
        return player2Score;
    }

    public void setPlayer2Score(int player2Score) {
        this.player2Score = player2Score;
    }

    public int getPlayer2Tokens() {
        return player2Tokens;
    }

    public void setPlayer2Tokens(int player2Tokens) {
        this.player2Tokens = player2Tokens;
    }

    public List<CardDTO> getPlayer2Timeline() {
        return player2Timeline;
    }

    public void setPlayer2Timeline(List<CardDTO> player2Timeline) {
        this.player2Timeline = player2Timeline;
    }

    public static class CurrentSongDTO {
        private String audioUrl;
        private boolean isDetailsHidden;
        private String title;
        private String artist;
        private Integer year;

        public CurrentSongDTO() {
        }

        public String getAudioUrl() {
            return audioUrl;
        }

        public void setAudioUrl(String audioUrl) {
            this.audioUrl = audioUrl;
        }

        public boolean isDetailsHidden() {
            return isDetailsHidden;
        }

        public void setDetailsHidden(boolean detailsHidden) {
            isDetailsHidden = detailsHidden;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getArtist() {
            return artist;
        }

        public void setArtist(String artist) {
            this.artist = artist;
        }

        public Integer getYear() {
            return year;
        }

        public void setYear(Integer year) {
            this.year = year;
        }
    }

    public static class CardDTO {
        private Long songId;
        private int year;
        private String artist;
        private String title;

        public CardDTO() {
        }

        public Long getSongId() {
            return songId;
        }

        public void setSongId(Long songId) {
            this.songId = songId;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public String getArtist() {
            return artist;
        }

        public void setArtist(String artist) {
            this.artist = artist;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}