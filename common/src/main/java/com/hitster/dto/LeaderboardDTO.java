package com.hitster.dto;

import java.util.List;

public class LeaderboardDTO {

    private List<LeaderboardEntryDTO> entries;

    public LeaderboardDTO() {
    }

    public LeaderboardDTO(List<LeaderboardEntryDTO> entries) {
        this.entries = entries;
    }

    public List<LeaderboardEntryDTO> getEntries() {
        return entries;
    }

    public void setEntries(List<LeaderboardEntryDTO> entries) {
        this.entries = entries;
    }
}