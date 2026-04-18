package com.hitster.dto;

public class LeaderboardEntryDTO {

    private Long userId;
    private String username;
    private Integer totalWinnings;
    private String profilePicturePath;

    public LeaderboardEntryDTO() {
    }

    public LeaderboardEntryDTO(Long userId, String username, Integer totalWinnings, String profilePicturePath) {
        this.userId = userId;
        this.username = username;
        this.totalWinnings = totalWinnings;
        this.profilePicturePath = profilePicturePath;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getTotalWinnings() {
        return totalWinnings;
    }

    public void setTotalWinnings(Integer totalWinnings) {
        this.totalWinnings = totalWinnings;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }
}