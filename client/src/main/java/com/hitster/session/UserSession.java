package com.hitster.session;

public class UserSession {

    private static UserSession instance;

    private Long userId;
    private String userName;
    private String token;
    private boolean isAdmin;


    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public boolean getIsAdmin() { return isAdmin; }
    public void setIsAdmin(boolean isAdmin) { this.isAdmin = isAdmin; }


 
    public void cleanUserSession() {
        this.userId = null;
        this.userName = null;
        this.token = null;
        this.isAdmin = false;

    }
}