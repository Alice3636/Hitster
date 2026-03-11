package com.hitster.model;

public class LastTurnData {

    private final Player actingPlayer;
    private final Song playedSong;
    private final int insertPosition;
    private final boolean placementCorrect;

    public LastTurnData(Player actingPlayer, Song playedSong, int insertPosition, boolean placementCorrect) {
        this.actingPlayer = actingPlayer;
        this.playedSong = playedSong;
        this.insertPosition = insertPosition;
        this.placementCorrect = placementCorrect;
    }

    public Player getActingPlayer() {
        return actingPlayer;
    }

    public Song getPlayedSong() {
        return playedSong;
    }

    public int getInsertPosition() {
        return insertPosition;
    }

    public boolean isPlacementCorrect() {
        return placementCorrect;
    }
}