package model;

public class TurnResult {
    private final boolean placementCorrect;
    private final boolean titleAndArtistCorrect;
    private final boolean pointAwarded;
    private final boolean tokenAwarded;
    private final String message;

    public TurnResult(boolean placementCorrect,
                      boolean titleAndArtistCorrect,
                      boolean pointAwarded,
                      boolean tokenAwarded,
                      String message) {
        this.placementCorrect = placementCorrect;
        this.titleAndArtistCorrect = titleAndArtistCorrect;
        this.pointAwarded = pointAwarded;
        this.tokenAwarded = tokenAwarded;
        this.message = message;
    }

    public boolean isPlacementCorrect() {
        return placementCorrect;
    }

    public boolean isTitleAndArtistCorrect() {
        return titleAndArtistCorrect;
    }

    public boolean isPointAwarded() {
        return pointAwarded;
    }

    public boolean isTokenAwarded() {
        return tokenAwarded;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "TurnResult{" +
                "placementCorrect=" + placementCorrect +
                ", titleAndArtistCorrect=" + titleAndArtistCorrect +
                ", pointAwarded=" + pointAwarded +
                ", tokenAwarded=" + tokenAwarded +
                ", message='" + message + '\'' +
                '}';
    }
}