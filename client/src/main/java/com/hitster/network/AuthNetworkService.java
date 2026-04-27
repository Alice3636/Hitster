package com.hitster.network;

import com.hitster.dto.auth.ForgotPasswordRequestDTO;
import com.hitster.dto.auth.LoginRequestDTO;
import com.hitster.dto.auth.RegisterRequestDTO;
import com.hitster.dto.auth.ResetPasswordRequestDTO;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Provides client-side access to authentication and password recovery endpoints.
 */
public class AuthNetworkService {

    private final ApiClient apiClient;

    /**
     * Creates an authentication service backed by the shared API client.
     */
    public AuthNetworkService() {
        this.apiClient = new ApiClient();
    }

    /**
     * Registers a new user account.
     *
     * @param username requested username
     * @param email user email address
     * @param password raw password entered by the user
     * @param picturePath optional profile picture path
     * @return asynchronous HTTP response for the registration request
     */
    public CompletableFuture<HttpResponse<String>> register(String username, String email, String password,
            String picturePath) {
        RegisterRequestDTO requestDTO = new RegisterRequestDTO(username, email, password, picturePath);
        return apiClient.post("/auth/register", requestDTO);
    }

    /**
     * Starts the forgot-password flow for the supplied email address.
     *
     * @param email account email address
     * @return asynchronous HTTP response for the forgot-password request
     */
    public CompletableFuture<HttpResponse<String>> forgotPassword(String email) {
        ForgotPasswordRequestDTO requestDTO = new ForgotPasswordRequestDTO(email);
        return apiClient.post("/auth/forgot-password", requestDTO);
    }

    /**
     * Authenticates a user and requests a session token.
     *
     * @param email account email address
     * @param password raw password entered by the user
     * @return asynchronous HTTP response containing login details on success
     */
    public CompletableFuture<HttpResponse<String>> login(String email, String password) {
        LoginRequestDTO requestDTO = new LoginRequestDTO(email, password);
        return apiClient.post("/auth/login", requestDTO);
    }

    /**
     * Completes the password reset flow using the verification code sent by email.
     *
     * @param email account email address
     * @param code verification code supplied by the server
     * @param newPassword replacement password entered by the user
     * @return asynchronous HTTP response for the reset request
     */
    public CompletableFuture<HttpResponse<String>> resetPassword(String email, String code, String newPassword) {
        ResetPasswordRequestDTO requestDTO = new ResetPasswordRequestDTO(email, code, newPassword);

        return apiClient.post("/auth/reset-password", requestDTO);
    }
}
