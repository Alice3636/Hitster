package com.hitster.network;

import com.hitster.dto.admin.AddSongRequestDTO;
import com.hitster.dto.admin.DeleteSongsRequestDTO;
import com.hitster.dto.admin.DeleteUsersRequestDTO;
import com.hitster.dto.admin.UpdateSongRequestDTO;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AdminNetworkService {

    private final ApiClient apiClient;

    public AdminNetworkService() {
        this.apiClient = new ApiClient();
    }

    public CompletableFuture<HttpResponse<String>> getAllUsers() {
        return apiClient.get("/admin/users");
    }

    public CompletableFuture<HttpResponse<String>> deleteUsers(List<Long> userIds) {
        DeleteUsersRequestDTO requestDTO = new DeleteUsersRequestDTO(userIds);
        return apiClient.delete("/admin/users", requestDTO);
    }

    public CompletableFuture<HttpResponse<String>> getAllSongs() {
        return apiClient.get("/admin/songs");
    }

    public CompletableFuture<HttpResponse<String>> addSong(AddSongRequestDTO requestDTO) {
        return apiClient.postMultipart(
                "/admin/songs",
                List.of(
                        new ApiClient.MultipartTextPart("title", requestDTO.title()),
                        new ApiClient.MultipartTextPart("artist", requestDTO.artist()),
                        new ApiClient.MultipartTextPart("releaseYear", String.valueOf(requestDTO.releaseYear()))
                ),
                new ApiClient.MultipartFilePart("file", requestDTO.audioFile())
        );
    }

    public CompletableFuture<HttpResponse<String>> updateSong(long songId, UpdateSongRequestDTO requestDTO) {
        return apiClient.put("/admin/songs/" + songId, requestDTO);
    }

    public CompletableFuture<HttpResponse<String>> deleteSongs(List<Long> songIds) {
        DeleteSongsRequestDTO requestDTO = new DeleteSongsRequestDTO(songIds);
        return apiClient.delete("/admin/songs", requestDTO);
    }
}
