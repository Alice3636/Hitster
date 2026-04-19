// NOT READY

package com.hitster.dto.game;

public class PlaceSongRequestDTO {

    private int index_position;
    private Long songId;

    public PlaceSongRequestDTO() {
    }

    public int getIndex_position() {
        return index_position;
    }

    public void setIndex_position(int index_position) {
        this.index_position = index_position;
    }

    public Long getSongId() {
        return songId;
    }

    public void setSongId(Long songId) {
        this.songId = songId;
    }
}