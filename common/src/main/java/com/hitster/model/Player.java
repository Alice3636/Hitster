package com.hitster.model;

public class Player {
    private final int id;
    private String username;
    private int score;
    private int tokens;
    private boolean connected;

    public Player(int id, String username) {
        this.id = id;
        this.username = username;
        this.score = 0;
        this.tokens = 0;
        this.connected = true;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getScore() {
        return score;
    }

    public void addPoint() {
        this.score++;
    }

    public int getTokens() {
        return tokens;
    }

    public void addToken() {
        this.tokens++;
    }

    public boolean useToken() {
        if (tokens <= 0) {
            return false;
        }
        tokens--;
        return true;
    }

    public boolean isConnected() {
        return connected;
    }

    public void disconnect() {
        this.connected = false;
    }

    public void reconnect() {
        this.connected = true;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", score=" + score +
                ", tokens=" + tokens +
                ", connected=" + connected +
                '}';
    }
}