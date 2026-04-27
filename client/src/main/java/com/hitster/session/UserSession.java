package com.hitster.session;

/**
 * Stores authenticated user details for the current client process.
 */
public class UserSession {

    private static UserSession instance;

    private Long userId;
    private String userName;
    private String token;
    private boolean isAdmin;


    private UserSession() {}

    /**
     * Returns the singleton session instance used by controllers and services.
     *
     * @return current user session instance
     */
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    /**
     * Returns the authenticated user's identifier.
     *
     * @return current user id, or {@code null} when no user is logged in
     */
    public Long getUserId() { return userId; }

    /**
     * Updates the authenticated user's identifier.
     *
     * @param userId user id received from the server
     */
    public void setUserId(Long userId) { this.userId = userId; }

    /**
     * Returns the authenticated user's display name.
     *
     * @return current username, or {@code null} when no user is logged in
     */
    public String getUserName() { return userName; }

    /**
     * Updates the authenticated user's display name.
     *
     * @param userName username received from the server
     */
    public void setUserName(String userName) { this.userName = userName; }

    /**
     * Returns the authentication token used for authorized requests.
     *
     * @return bearer token, or {@code null} when no user is logged in
     */
    public String getToken() { return token; }

    /**
     * Updates the authentication token used for authorized requests.
     *
     * @param token bearer token received from the server
     */
    public void setToken(String token) { this.token = token; }

    /**
     * Indicates whether the authenticated user has administrator privileges.
     *
     * @return {@code true} when the current user is an administrator
     */
    public boolean getIsAdmin() { return isAdmin; }

    /**
     * Updates the administrator flag for the authenticated user.
     *
     * @param isAdmin whether the current user is an administrator
     */
    public void setIsAdmin(boolean isAdmin) { this.isAdmin = isAdmin; }


 
    /**
     * Clears all authenticated user data when the user logs out or the account is deleted.
     */
    public void cleanUserSession() {
        this.userId = null;
        this.userName = null;
        this.token = null;
        this.isAdmin = false;

    }
}
