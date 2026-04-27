package com.hitster.controller;

import com.hitster.dto.admin.CreateSongRequestDTO;
import com.hitster.dto.admin.DeleteSongsRequestDTO;
import com.hitster.dto.admin.DeleteUsersRequestDTO;
import com.hitster.dto.admin.SongsResponseDTO;
import com.hitster.dto.admin.UpdateSongRequestDTO;
import com.hitster.dto.admin.UsersResponseDTO;
import com.hitster.dto.game.SongDTO;
import com.hitster.service.DatabaseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/users")
    public UsersResponseDTO getAllUsers(HttpServletRequest request) {
        requireAdmin(request);
        return new UsersResponseDTO(DatabaseService.getAllUsers());
    }

    @DeleteMapping("/users")
    public String deleteUsers(@RequestBody DeleteUsersRequestDTO request,
                              HttpServletRequest httpRequest) {
        requireAdmin(httpRequest);

        for (Long id : request.userIds()) {
            DatabaseService.deleteUserById(id);
        }

        return "OK";
    }

    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable Long id, HttpServletRequest request) {
        requireAdmin(request);

        boolean deleted = DatabaseService.deleteUserById(id);
        if (!deleted) {
            throw new NotFoundException("User not found: " + id);
        }

        return "OK";
    }

    @GetMapping("/songs")
    public SongsResponseDTO getAllSongs(HttpServletRequest request) {
        requireAdmin(request);
        return new SongsResponseDTO(DatabaseService.getAllSongs());
    }

    @PostMapping(value = "/songs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SongDTO uploadSong(@ModelAttribute CreateSongRequestDTO request,
                              HttpServletRequest httpRequest) {
        requireAdmin(httpRequest);

        if (request == null ||
                isBlank(request.getTitle()) ||
                isBlank(request.getArtist()) ||
                request.getReleaseYear() <= 0) {
            throw new IllegalArgumentException("Title, artist, and releaseYear are required.");
        }

        if (request.getFile() == null || request.getFile().isEmpty()) {
            throw new IllegalArgumentException("Audio file is required.");
        }

        String originalFilename = StringUtils.cleanPath(
                request.getFile().getOriginalFilename() == null
                        ? ""
                        : request.getFile().getOriginalFilename()
        );

        if (originalFilename.isEmpty()) {
            throw new IllegalArgumentException("Invalid file name.");
        }

        String extension = extractExtension(originalFilename);
        if (extension.isEmpty()) {
            throw new IllegalArgumentException("File must have an extension.");
        }

        String lowerExt = extension.toLowerCase();
        if (!lowerExt.equals(".mp3") && !lowerExt.equals(".wav") && !lowerExt.equals(".ogg")) {
            throw new IllegalArgumentException("Only .mp3, .wav, or .ogg files are allowed.");
        }

        Path audioDir = resolveAudioUploadDir();

        try {
            Files.createDirectories(audioDir);

            String cleanOriginalFilename = originalFilename.replaceAll("\\s+", "_");
            String storedFilename = UUID.randomUUID() + "_" + cleanOriginalFilename;
            Path targetFile = audioDir.resolve(storedFilename).normalize();

            Files.copy(request.getFile().getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            String audioUrl = "/audio/" + storedFilename;

            Long newId = DatabaseService.createSong(
                    request.getTitle().trim(),
                    request.getArtist().trim(),
                    request.getReleaseYear(),
                    audioUrl
            );

            if (newId == null) {
                try {
                    Files.deleteIfExists(targetFile);
                } catch (IOException ignored) {
                }
                throw new IllegalArgumentException("Song creation failed.");
            }

            return new SongDTO(
                    newId,
                    request.getTitle().trim(),
                    request.getArtist().trim(),
                    request.getReleaseYear(),
                    audioUrl
            );

        } catch (IOException e) {
            throw new RuntimeException("Failed to store audio file.");
        }
    }

    @PutMapping(value = "/songs/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SongDTO updateSongJson(@PathVariable Long id,
                                  @RequestBody UpdateSongRequestDTO request,
                                  HttpServletRequest httpRequest) {
        return updateSong(id, request, httpRequest);
    }

    @PutMapping(value = "/songs/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SongDTO updateSongMultipart(@PathVariable Long id,
                                       @ModelAttribute UpdateSongRequestDTO request,
                                       HttpServletRequest httpRequest) {
        return updateSong(id, request, httpRequest);
    }

    private SongDTO updateSong(Long id,
                               UpdateSongRequestDTO request,
                               HttpServletRequest httpRequest) {
        requireAdmin(httpRequest);

        if (request == null ||
                isBlank(request.getTitle()) ||
                isBlank(request.getArtist()) ||
                request.getReleaseYear() <= 0 ||
                isBlank(request.getAudioUrl())) {
            throw new IllegalArgumentException("Title, artist, releaseYear, and audioUrl are required.");
        }

        boolean updated = DatabaseService.updateSong(
                id,
                request.getTitle().trim(),
                request.getArtist().trim(),
                request.getReleaseYear(),
                request.getAudioUrl().trim()
        );

        if (!updated) {
            throw new NotFoundException("Song not found: " + id);
        }

        return new SongDTO(
                id,
                request.getTitle().trim(),
                request.getArtist().trim(),
                request.getReleaseYear(),
                request.getAudioUrl().trim()
        );
    }

    @DeleteMapping("/songs")
    public String deleteSongs(@RequestBody DeleteSongsRequestDTO request,
                              HttpServletRequest httpRequest) {
        requireAdmin(httpRequest);

        for (Long id : request.songIds()) {
            DatabaseService.deleteSongById(id);
        }

        return "OK";
    }

    @DeleteMapping("/songs/{id}")
    public String deleteSong(@PathVariable Long id, HttpServletRequest request) {
        requireAdmin(request);

        List<SongDTO> songs = DatabaseService.getAllSongs();
        SongDTO songToDelete = songs.stream()
                .filter(song -> song.songId() != null && song.songId().equals(id))
                .findFirst()
                .orElse(null);

        boolean deleted = DatabaseService.deleteSongById(id);
        if (!deleted) {
            throw new NotFoundException("Song not found: " + id);
        }

        if (songToDelete != null && !isBlank(songToDelete.audioUrl())) {
            deletePhysicalAudioFile(songToDelete.audioUrl());
        }

        return "OK";
    }

    private void requireAdmin(HttpServletRequest request) {
        Object jwtIsAdminObj = request.getAttribute("jwtIsAdmin");

        if (!(jwtIsAdminObj instanceof Boolean) || !((Boolean) jwtIsAdminObj)) {
            throw new SecurityException("Admin access required.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) {
            return "";
        }
        return filename.substring(dotIndex);
    }

    private void deletePhysicalAudioFile(String audioUrl) {
        String filename = audioUrl.replace("/audio/", "").trim();
        if (filename.isEmpty()) {
            return;
        }

        Path audioDir = resolveAudioUploadDir();
        Path filePath = audioDir.resolve(filename).normalize();

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
    }

    private Path resolveAudioUploadDir() {
        Path cwd = Paths.get("").toAbsolutePath();

        Path option1 = cwd.resolve(Paths.get("server", "uploads", "audio")).normalize();
        if (Files.exists(option1.getParent())) {
            return option1;
        }

        Path option2 = cwd.resolve(Paths.get("Hitster-Yoni-dev-alice-merged", "server", "uploads", "audio")).normalize();
        if (Files.exists(option2.getParent())) {
            return option2;
        }

        return option1;
    }
}
