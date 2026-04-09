package model;

public class TurnAction {
    private final int insertPosition;
    private final String guessedTitle;
    private final String guessedArtist;

    public TurnAction(int insertPosition, String guessedTitle, String guessedArtist) {
        this.insertPosition = insertPosition;
        this.guessedTitle = guessedTitle;
        this.guessedArtist = guessedArtist;
    }

    public int getInsertPosition() {
        return insertPosition;
    }

    public String getGuessedTitle() {
        return guessedTitle;
    }

    public String getGuessedArtist() {
        return guessedArtist;
    }
}