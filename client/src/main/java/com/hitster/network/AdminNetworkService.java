package com.hitster.network;

import com.google.gson.Gson;
import com.hitster.config.AppConfig;
import com.hitster.dto.admin.DeleteSongsRequestDTO;
import com.hitster.dto.admin.DeleteUsersRequestDTO;
import com.hitster.session.UserSession;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AdminNetworkService {
    private final HttpClient httpClient;

    public AdminNetworkService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public CompletableFuture<HttpResponse<String>> getAllUsers() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/admin/users"))
                .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> deleteUsers(List<Long> userIds) {
        String token = UserSession.getInstance().getToken();
        DeleteUsersRequestDTO deleteUsersRequest = new DeleteUsersRequestDTO(userIds);

        Gson gson = new Gson();
        String jsonPayload = gson.toJson(deleteUsersRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/admin/users"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .method("DELETE", HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> getAllSongs() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/admin/songs"))
                .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> addSong(
            String title,
            String artist,
            int releaseYear,
            Path audioFile
    ) {
        String boundary = "----HitsterBoundary" + UUID.randomUUID();

        HttpRequest.BodyPublisher bodyPublisher;
        try {
            bodyPublisher = buildMultipartBodyForAddSong(boundary, title, artist, releaseYear, audioFile);
        } catch (IOException e) {
            CompletableFuture<HttpResponse<String>> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/admin/songs"))
                .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(bodyPublisher)
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> updateSong(
            long songId,
            String title,
            String artist,
            int releaseYear,
            String audioUrl
    ) {
        String boundary = "----HitsterBoundary" + UUID.randomUUID();

        HttpRequest.BodyPublisher bodyPublisher = buildMultipartBodyForUpdateSong(
                boundary,
                title,
                artist,
                releaseYear,
                audioUrl
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/admin/songs/" + songId))
                .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .PUT(bodyPublisher)
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> deleteSongs(List<Long> songIds) {
        String token = UserSession.getInstance().getToken();
        DeleteSongsRequestDTO deleteSongsRequest = new DeleteSongsRequestDTO(songIds);

        Gson gson = new Gson();
        String jsonPayload = gson.toJson(deleteSongsRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/admin/songs"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .method("DELETE", HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest.BodyPublisher buildMultipartBodyForAddSong(
            String boundary,
            String title,
            String artist,
            int releaseYear,
            Path audioFile
    ) throws IOException {
        if (audioFile == null) {
            throw new IOException("Audio file is required.");
        }

        String filename = audioFile.getFileName().toString();
        String mimeType = Files.probeContentType(audioFile);
        if (mimeType == null || mimeType.isBlank()) {
            mimeType = "application/octet-stream";
        }

        byte[] fileBytes = Files.readAllBytes(audioFile);

        return HttpRequest.BodyPublishers.ofByteArrays(List.of(
                textPart(boundary, "title", title),
                textPart(boundary, "artist", artist),
                textPart(boundary, "releaseYear", String.valueOf(releaseYear)),
                filePartHeader(boundary, "file", filename, mimeType),
                fileBytes,
                lineBreak(),
                closingBoundary(boundary)
        ));
    }

    private HttpRequest.BodyPublisher buildMultipartBodyForUpdateSong(
            String boundary,
            String title,
            String artist,
            int releaseYear,
            String audioUrl
    ) {
        return HttpRequest.BodyPublishers.ofByteArrays(List.of(
                textPart(boundary, "title", title),
                textPart(boundary, "artist", artist),
                textPart(boundary, "releaseYear", String.valueOf(releaseYear)),
                textPart(boundary, "audioUrl", audioUrl),
                closingBoundary(boundary)
        ));
    }

    private byte[] textPart(String boundary, String name, String value) {
        String part = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n"
                + (value == null ? "" : value)
                + "\r\n";

        return part.getBytes();
    }

    private byte[] filePartHeader(String boundary, String name, String filename, String mimeType) {
        String part = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n"
                + "Content-Type: " + mimeType + "\r\n\r\n";

        return part.getBytes();
    }

    private byte[] lineBreak() {
        return "\r\n".getBytes();
    }

    private byte[] closingBoundary(String boundary) {
        return ("--" + boundary + "--\r\n").getBytes();
    }
}