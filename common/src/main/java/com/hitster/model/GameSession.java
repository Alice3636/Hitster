package com.hitster.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class GameSession {
    private final int id;
    private final Player player1;
    private final Player player2;

    private final List<SongCard> player1Timeline;
    private final List<SongCard> player2Timeline;
    private final Queue<Song> remainingSongs;

    private Song currentSong;
    private Player currentTurnPlayer;
    private GameStatus status;
    private GamePhase phase;
    private Player winner;
    private LastTurnData lastTurnData;
    private int turnNumber;

    public GameSession(int id, Player player1, Player player2, List<Song> songsPool) {
        this.id = id;
        this.player1 = player1;
        this.player2 = player2;
        this.player1Timeline = new ArrayList<>();
        this.player2Timeline = new ArrayList<>();
        this.remainingSongs = new LinkedList<>(songsPool);
        this.status = GameStatus.WAITING_FOR_PLAYERS;
        this.phase = GamePhase.WAITING_FOR_PLAYERS;
        this.turnNumber = 0;
    }

    public int getId() {
        return id;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public List<SongCard> getPlayer1Timeline() {
        return player1Timeline;
    }

    public List<SongCard> getPlayer2Timeline() {
        return player2Timeline;
    }

    public Queue<Song> getRemainingSongs() {
        return remainingSongs;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public void setCurrentSong(Song currentSong) {
        this.currentSong = currentSong;
    }

    public Player getCurrentTurnPlayer() {
        return currentTurnPlayer;
    }

    public void setCurrentTurnPlayer(Player currentTurnPlayer) {
        this.currentTurnPlayer = currentTurnPlayer;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public Player getWinner() {
        return winner;
    }

    public void setWinner(Player winner) {
        this.winner = winner;
    }

    public LastTurnData getLastTurnData() {
        return lastTurnData;
    }

    public void setLastTurnData(LastTurnData lastTurnData) {
        this.lastTurnData = lastTurnData;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public void incrementTurnNumber() {
        this.turnNumber++;
    }

    public List<SongCard> getTimelineOfPlayer(Player player) {
        if (player.getId() == (player1.getId())) {
            return player1Timeline;
        }
        return player2Timeline;
    }

    public Player getOpponent(Player player) {
        if (player.getId() == (player1.getId())) {
            return player2;
        }
        return player1;
    }

    public Player getPlayerById(int playerId) {
        if (player1.getId() == (playerId)) {
            return player1;
        }
        if (player2.getId() == (playerId)) {
            return player2;
        }
        return null;
    }

    public boolean isFinished() {
        return status == GameStatus.FINISHED;
    }
}