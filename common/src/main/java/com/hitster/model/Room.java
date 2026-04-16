package com.hitster.model;

public class Room {
    private final int id;
    private Player player1;
    private Player player2;
    private boolean started;
    private GameSession gameSession;

    public Room(int id, Player player1) {
        this.id = id;
        this.player1 = player1;
        this.started = false;
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

    public boolean isFull() {
        return player1 != null && player2 != null;
    }

    public boolean isStarted() {
        return started;
    }

    public GameSession getGameSession() {
        return gameSession;
    }

    public boolean addPlayer(Player player) {
        if (player2 != null) {
            return false;
        }
        player2 = player;
        return true;
    }

    public void startGame(GameSession gameSession) {
        this.gameSession = gameSession;
        this.started = true;
    }

    @Override
    public String toString() {
        return "Room{" +
                "id='" + id + '\'' +
                ", player1=" + (player1 != null ? player1.getUsername() : "null") +
                ", player2=" + (player2 != null ? player2.getUsername() : "null") +
                ", started=" + started +
                '}';
    }
}