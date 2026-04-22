package com.hitster.network;

import com.google.gson.Gson;
import com.hitster.config.AppConfig;
import com.hitster.dto.auth.ForgotPasswordRequestDTO;
import com.hitster.dto.auth.LoginRequestDTO;
import com.hitster.dto.auth.RegisterRequestDTO;
import com.hitster.session.UserSession;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class AuthNetworkService {

    private final HttpClient httpClient;

    public AuthNetworkService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public CompletableFuture<HttpResponse<String>> register(String username, String email, String password,
            String picturePath) {
        String token = UserSession.getInstance().getToken();
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(username, email, password, picturePath);

        Gson gson = new Gson();
        String jsonPayload = gson.toJson(registerRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/auth/register"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> forgotPassword(String email) {
        String token = UserSession.getInstance().getToken();
        ForgotPasswordRequestDTO forgotRequest = new ForgotPasswordRequestDTO(email);

        Gson gson = new Gson();
        String jsonPayload = gson.toJson(forgotRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/auth/forgot-password"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> login(String email, String password) {

        String token = UserSession.getInstance().getToken();
        LoginRequestDTO loginRequest = new LoginRequestDTO(email, password);

        Gson gson = new Gson();
        String jsonPayload = gson.toJson(loginRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
}
