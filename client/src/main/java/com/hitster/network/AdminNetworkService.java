package com.hitster.network;

import com.hitster.dto.admin.AddSongRequestDTO;
import com.hitster.dto.admin.DeleteSongsRequestDTO;
import com.hitster.dto.admin.DeleteUsersRequestDTO;
import com.hitster.dto.admin.UpdateSongRequestDTO;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Provides client-side access to administrator user and song management endpoints.
 */
public class AdminNetworkService {

    private final ApiClient apiClient;

    /**
     * Creates an administrator network service backed by the shared API client.
     */
    public AdminNetworkService() {
        this.apiClient = new ApiClient();
    }

    /**
     * Fetches all users visible to administrators.
     *
     * @return asynchronous HTTP response containing a users payload
     */
    public CompletableFuture<HttpResponse<String>> getAllUsers() {
        return apiClient.get("/admin/users");
    }

    /**
     * Requests deletion of the selected user accounts.
     *
     * @param userIds user identifiers to delete
     * @return asynchronous HTTP response for the delete request
     */
    public CompletableFuture<HttpResponse<String>> deleteUsers(List<Long> userIds) {
        DeleteUsersRequestDTO requestDTO = new DeleteUsersRequestDTO(userIds);
        return apiClient.delete("/admin/users", requestDTO);
    }

    /**
     * Fetches all songs visible to administrators.
     *
     * @return asynchronous HTTP response containing a songs payload
     */
    public CompletableFuture<HttpResponse<String>> getAllSongs() {
        return apiClient.get("/admin/songs");
    }

    /**
     * Uploads a new song and its audio file to the server.
     *
     * @param requestDTO song metadata and local audio file path
     * @return asynchronous HTTP response for the upload request
     */
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

    /**
     * Updates metadata for an existing song.
     *
     * @param songId identifier of the song to update
     * @param requestDTO updated song metadata
     * @return asynchronous HTTP response for the update request
     */
    public CompletableFuture<HttpResponse<String>> updateSong(long songId, UpdateSongRequestDTO requestDTO) {
        return apiClient.put("/admin/songs/" + songId, requestDTO);
    }

    /**
     * Requests deletion of the selected songs.
     *
     * @param songIds song identifiers to delete
     * @return asynchronous HTTP response for the delete request
     */
    public CompletableFuture<HttpResponse<String>> deleteSongs(List<Long> songIds) {
        DeleteSongsRequestDTO requestDTO = new DeleteSongsRequestDTO(songIds);
        return apiClient.delete("/admin/songs", requestDTO);
    }
}
