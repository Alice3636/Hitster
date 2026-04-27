package com.hitster.network;

import com.google.gson.Gson;
import com.hitster.config.AppConfig;
import com.hitster.session.UserSession;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ApiClient {

    private final HttpClient httpClient;
    private final Gson gson;

    public ApiClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public CompletableFuture<HttpResponse<String>> get(String endpoint) {
        HttpRequest request = baseRequest(endpoint).GET().build();
        return send(request);
    }

    public CompletableFuture<HttpResponse<String>> post(String endpoint) {
        HttpRequest request = baseRequest(endpoint).POST(HttpRequest.BodyPublishers.noBody()).build();
        return send(request);
    }

    public CompletableFuture<HttpResponse<String>> post(String endpoint, Object body) {
        HttpRequest request = baseJsonRequest(endpoint)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body), StandardCharsets.UTF_8))
                .build();
        return send(request);
    }

    public CompletableFuture<HttpResponse<String>> put(String endpoint, Object body) {
        HttpRequest request = baseJsonRequest(endpoint)
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(body), StandardCharsets.UTF_8))
                .build();
        return send(request);
    }

    public CompletableFuture<HttpResponse<String>> delete(String endpoint) {
        HttpRequest request = baseRequest(endpoint).DELETE().build();
        return send(request);
    }

    public CompletableFuture<HttpResponse<String>> delete(String endpoint, Object body) {
        HttpRequest request = baseJsonRequest(endpoint)
                .method("DELETE", HttpRequest.BodyPublishers.ofString(gson.toJson(body), StandardCharsets.UTF_8))
                .build();
        return send(request);
    }

    public CompletableFuture<HttpResponse<String>> postMultipart(String endpoint, List<MultipartTextPart> textParts, MultipartFilePart filePart) {
        try {
            String boundary = createBoundary();
            HttpRequest request = baseRequest(endpoint)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(buildMultipartBody(boundary, textParts, filePart))
                    .build();
            return send(request);
        } catch (IOException e) {
            return failedFuture(e);
        }
    }

    public CompletableFuture<HttpResponse<String>> putMultipart(String endpoint, List<MultipartTextPart> textParts) {
        try {
            String boundary = createBoundary();
            HttpRequest request = baseRequest(endpoint)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .PUT(buildMultipartBody(boundary, textParts, null))
                    .build();
            return send(request);
        } catch (IOException e) {
            return failedFuture(e);
        }
    }

    private HttpRequest.BodyPublisher buildMultipartBody(String boundary, List<MultipartTextPart> textParts, MultipartFilePart filePart) throws IOException {
        List<byte[]> parts = new ArrayList<>();

        if (textParts != null) {
            for (MultipartTextPart textPart : textParts) {
                parts.add(textPart(boundary, textPart.name(), textPart.value()));
            }
        }

        if (filePart != null) {
            Path path = filePart.path();
            if (path == null) {
                throw new IOException("Multipart file path is required.");
            }

            String filename = path.getFileName().toString();
            String mimeType = Files.probeContentType(path);
            if (mimeType == null || mimeType.isBlank()) {
                mimeType = "application/octet-stream";
            }

            parts.add(filePartHeader(boundary, filePart.name(), filename, mimeType));
            parts.add(Files.readAllBytes(path));
            parts.add(lineBreak());
        }

        parts.add(closingBoundary(boundary));
        return HttpRequest.BodyPublishers.ofByteArrays(parts);
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
        return baseRequest(endpoint).header("Content-Type", "application/json; charset=UTF-8");
    }

    private CompletableFuture<HttpResponse<String>> send(HttpRequest request) {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private CompletableFuture<HttpResponse<String>> failedFuture(Exception exception) {
        CompletableFuture<HttpResponse<String>> future = new CompletableFuture<>();
        future.completeExceptionally(exception);
        return future;
    }

    private String createBoundary() {
        return "----HitsterBoundary" + UUID.randomUUID();
    }

    private byte[] textPart(String boundary, String name, String value) {
        String part = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + name + "\"\r\n"
                + "Content-Type: text/plain; charset=UTF-8\r\n\r\n"
                + (value == null ? "" : value)
                + "\r\n";
        return part.getBytes(StandardCharsets.UTF_8);
    }

    private byte[] filePartHeader(String boundary, String name, String filename, String mimeType) {
        String part = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n"
                + "Content-Type: " + mimeType + "\r\n\r\n";
        return part.getBytes(StandardCharsets.UTF_8);
    }

    private byte[] lineBreak() {
        return "\r\n".getBytes(StandardCharsets.UTF_8);
    }

    private byte[] closingBoundary(String boundary) {
        return ("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);
    }

    public record MultipartTextPart(String name, String value) {}

    public record MultipartFilePart(String name, Path path) {}
}
