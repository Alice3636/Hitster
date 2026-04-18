package com.hitster.dto;

public class AuthResponseDTO {
    private Long userId;
    private String username;
    private boolean isAdmin;
    private String token;

    public AuthResponseDTO() {}

    public AuthResponseDTO(Long userId, String username, boolean isAdmin, String token) {
        this.userId = userId;
        this.username = username;
        this.isAdmin = isAdmin;
        this.token = token;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}