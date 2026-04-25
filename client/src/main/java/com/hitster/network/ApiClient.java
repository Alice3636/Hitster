package com.hitster.network;

import com.google.gson.Gson;
import com.hitster.config.AppConfig;
import com.hitster.session.UserSession;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class ApiClient {

    private final HttpClient httpClient;
    private final Gson gson;

    public ApiClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public CompletableFuture<HttpResponse<String>> get(String endpoint) {
        HttpRequest request = baseRequest(endpoint)
                .GET()
                .build();

        return send(request);
    }

    public CompletableFuture<HttpResponse<String>> post(String endpoint) {
        HttpRequest request = baseRequest(endpoint)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return send(request);
    }

    public CompletableFuture<HttpResponse<String>> post(String endpoint, Object body) {
        HttpRequest request = baseJsonRequest(endpoint)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        return send(request);
    }

    public CompletableFuture<HttpResponse<String>> put(String endpoint, Object body) {
        HttpRequest request = baseJsonRequest(endpoint)
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        return send(request);
    }

    public CompletableFuture<HttpResponse<String>> delete(String endpoint) {
        HttpRequest request = baseRequest(endpoint)
                .DELETE()
                .build();

        return send(request);
    }

    public CompletableFuture<HttpResponse<String>> delete(String endpoint, Object body) {
        HttpRequest request = baseJsonRequest(endpoint)
                .method("DELETE", HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        return send(request);
    }

    private HttpRequest.Builder baseRequest(String endpoint) {
        String token = UserSession.getInstance().getToken();

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + endpoint));

        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }

        return builder;
    }

    private HttpRequest.Builder baseJsonRequest(String endpoint) {
        return baseRequest(endpoint)
                .header("Content-Type", "application/json");
    }

    private CompletableFuture<HttpResponse<String>> send(HttpRequest request) {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
}
