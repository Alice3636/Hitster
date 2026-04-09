package model;

public class SongCard {
    private final Song song;

    public SongCard(Song song) {
        this.song = song;
    }

    public Song getSong() {
        return song;
    }

    @Override
    public String toString() {
        return "SongCard{" +
                "song=" + song +
                '}';
    }
}