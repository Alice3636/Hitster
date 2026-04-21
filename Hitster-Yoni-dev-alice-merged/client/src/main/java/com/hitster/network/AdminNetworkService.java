package com.hitster.network;

import com.hitster.config.AppConfig;
import com.hitster.session.UserSession;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

public class AdminNetworkService {

    private final HttpClient httpClient;

    public AdminNetworkService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public CompletableFuture<HttpResponse<String>> getAllSongs() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/admin/songs"))
                .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> uploadNewSong(String title, String artist, String year, File mp3File) throws IOException {
        String boundary = "---Boundary" + System.currentTimeMillis();
        byte[] fileBytes = Files.readAllBytes(mp3File.toPath());
        
        StringBuilder bodyBuilder = new StringBuilder();
        
        bodyBuilder.append("--").append(boundary).append("\r\n")
                .append("Content-Disposition: form-data; name=\"title\"\r\n\r\n")
                .append(title).append("\r\n");

        bodyBuilder.append("--").append(boundary).append("\r\n")
                .append("Content-Disposition: form-data; name=\"artist\"\r\n\r\n")
                .append(artist).append("\r\n");

        bodyBuilder.append("--").append(boundary).append("\r\n")
                .append("Content-Disposition: form-data; name=\"releaseYear\"\r\n\r\n")
                .append(year).append("\r\n");

        bodyBuilder.append("--").append(boundary).append("\r\n")
                .append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(mp3File.getName()).append("\"\r\n")
                .append("Content-Type: audio/mpeg\r\n\r\n");

        byte[] headerBytes = bodyBuilder.toString().getBytes();
        byte[] footerBytes = ("\r\n--" + boundary + "--\r\n").getBytes();

        byte[] fullBody = new byte[headerBytes.length + fileBytes.length + footerBytes.length];
        System.arraycopy(headerBytes, 0, fullBody, 0, headerBytes.length);
        System.arraycopy(fileBytes, 0, fullBody, headerBytes.length, fileBytes.length);
        System.arraycopy(footerBytes, 0, fullBody, headerBytes.length + fileBytes.length, footerBytes.length);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_API_URL + "/admin/songs"))
                .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(fullBody))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
}