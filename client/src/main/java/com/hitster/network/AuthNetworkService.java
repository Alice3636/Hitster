package com.hitster.network;

import com.hitster.dto.auth.ForgotPasswordRequestDTO;
import com.hitster.dto.auth.LoginRequestDTO;
import com.hitster.dto.auth.RegisterRequestDTO;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class AuthNetworkService {

    private final ApiClient apiClient;

    public AuthNetworkService() {
        this.apiClient = new ApiClient();
    }

    public CompletableFuture<HttpResponse<String>> register( String username, String email, String password, String picturePath) {
        RegisterRequestDTO requestDTO = new RegisterRequestDTO(username, email, password, picturePath);
        return apiClient.post("/auth/register", requestDTO);
    }

    public CompletableFuture<HttpResponse<String>> forgotPassword(String email) {
        ForgotPasswordRequestDTO requestDTO = new ForgotPasswordRequestDTO(email);
        return apiClient.post("/auth/forgot-password", requestDTO);
    }

    public CompletableFuture<HttpResponse<String>> login(String email, String password) {
        LoginRequestDTO requestDTO = new LoginRequestDTO(email, password);
        return apiClient.post("/auth/login", requestDTO);
    }
}
