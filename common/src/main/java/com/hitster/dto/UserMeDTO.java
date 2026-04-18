package com.hitster.dto;

public class UserMeDTO {

    private Long userId;
    private String username;
    private String email;
    private boolean admin;
    private Integer totalWinnings;
    private String profilePicturePath;

    public UserMeDTO() {
    }

    public UserMeDTO(Long userId, String username, String email, boolean admin,
                     Integer totalWinnings, String profilePicturePath) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.admin = admin;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
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