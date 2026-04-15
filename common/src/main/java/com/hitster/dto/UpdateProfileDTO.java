package com.hitster.dto;

public class UpdateProfileDTO {
    private String newUsername;
    private String newEmail;

    public UpdateProfileDTO(String newUsername, String newEmail) {
        this.newUsername = newUsername;
        this.newEmail = newEmail;
    }

    public String getNewUsername() { return newUsername; }
    public String getNewEmail() { return newEmail; }
    
    public void setNewUsername(String newUsername) { this.newUsername = newUsername; }
    public void setNewEmail(String newEmail) { this.newEmail = newEmail; }
}