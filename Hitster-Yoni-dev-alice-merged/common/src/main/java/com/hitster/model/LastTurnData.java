package com.hitster.model;

public class LastTurnData {

    private final Player actingPlayer;
    private final Song playedSong;
    private final int insertPosition;
    private final boolean placementCorrect;
    private boolean guessSubmitted;
    private boolean guessCorrect;
    private Integer challengedSuggestedIndex;

    public LastTurnData(Player actingPlayer, Song playedSong, int insertPosition, boolean placementCorrect) {
        this.actingPlayer = actingPlayer;
        this.playedSong = playedSong;
        this.insertPosition = insertPosition;
        this.placementCorrect = placementCorrect;
        this.guessSubmitted = false;
        this.guessCorrect = false;
        this.challengedSuggestedIndex = null;
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

    public boolean isGuessSubmitted() {
        return guessSubmitted;
    }

    public void setGuessSubmitted(boolean guessSubmitted) {
        this.guessSubmitted = guessSubmitted;
    }

    public boolean isGuessCorrect() {
        return guessCorrect;
    }

    public void setGuessCorrect(boolean guessCorrect) {
        this.guessCorrect = guessCorrect;
    }

    public Integer getChallengedSuggestedIndex() {
        return challengedSuggestedIndex;
    }

    public void setChallengedSuggestedIndex(Integer challengedSuggestedIndex) {
        this.challengedSuggestedIndex = challengedSuggestedIndex;
    }
}