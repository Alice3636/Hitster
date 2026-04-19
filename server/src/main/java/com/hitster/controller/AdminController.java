package com.hitster.controller;

import com.hitster.dto.AdminSongDTO;
import com.hitster.dto.AdminUserDTO;
import com.hitster.service.DatabaseService;
import com.hitster.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final FileStorageService fileStorageService;

    public AdminController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/users")
    public List<AdminUserDTO> getAllUsers(HttpServletRequest request) {
        requireAdmin(request);
        return DatabaseService.getAllUsers();
    }

    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable Long id, HttpServletRequest request) {
        requireAdmin(request);

        boolean deleted = DatabaseService.deleteUserById(id);
        if (!deleted) {
            throw new IllegalArgumentException("User not found: " + id);
        }

        return "OK";
    }

    @GetMapping("/songs")
    public List<AdminSongDTO> getAllSongs(HttpServletRequest request) {
        requireAdmin(request);
        return DatabaseService.getAllSongs();
    }

    @PostMapping(value = "/songs/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadSong(@RequestParam("file") MultipartFile file,
                             @RequestParam("title") String title,
                             @RequestParam("artist") String artist,
                             @RequestParam("year") int year,
                             HttpServletRequest request) throws Exception {
        requireAdmin(request);

        String audioUrl = fileStorageService.saveFile(file);
        Long songId = DatabaseService.createSong(title, artist, year, audioUrl);

        if (songId == null) {
            fileStorageService.deleteByAudioUrl(audioUrl);
            throw new IllegalStateException("Failed to insert song.");
        }

        return audioUrl;
    }

    @PutMapping("/songs/{id}")
    public String updateSong(@PathVariable Long id,
                             @RequestBody AdminSongDTO request,
                             HttpServletRequest httpRequest) {
        requireAdmin(httpRequest);

        AdminSongDTO existing = DatabaseService.getSongById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Song not found: " + id);
        }

        boolean updated = DatabaseService.updateSong(
                id,
                request.getTitle(),
                request.getArtist(),
                request.getYear(),
                existing.getAudioUrl()
        );

        if (!updated) {
            throw new IllegalArgumentException("Song not found: " + id);
        }

        return "OK";
    }

    @PutMapping(value = "/songs/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String replaceSongFile(@PathVariable Long id,
                                  @RequestParam("title") String title,
                                  @RequestParam("artist") String artist,
                                  @RequestParam("year") int year,
                                  @RequestParam(value = "file", required = false) MultipartFile file,
                                  HttpServletRequest request) throws Exception {
        requireAdmin(request);

        AdminSongDTO existing = DatabaseService.getSongById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Song not found: " + id);
        }

        String oldAudioUrl = existing.getAudioUrl();
        String newAudioUrl = oldAudioUrl;

        if (file != null && !file.isEmpty()) {
            newAudioUrl = fileStorageService.saveFile(file);
        }

        boolean updated = DatabaseService.updateSong(id, title, artist, year, newAudioUrl);

        if (!updated) {
            if (file != null && !file.isEmpty() && newAudioUrl != null && !newAudioUrl.equals(oldAudioUrl)) {
                fileStorageService.deleteByAudioUrl(newAudioUrl);
            }
            throw new IllegalArgumentException("Song not found: " + id);
        }

        if (file != null && !file.isEmpty() && oldAudioUrl != null && !oldAudioUrl.equals(newAudioUrl)) {
            fileStorageService.deleteByAudioUrl(oldAudioUrl);
        }

        return newAudioUrl;
    }

    @DeleteMapping("/songs/{id}")
    public String deleteSong(@PathVariable Long id, HttpServletRequest request) {
        requireAdmin(request);

        AdminSongDTO existing = DatabaseService.getSongById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Song not found: " + id);
        }

        boolean deleted = DatabaseService.deleteSongById(id);
        if (!deleted) {
            throw new IllegalArgumentException("Song not found: " + id);
        }

        fileStorageService.deleteByAudioUrl(existing.getAudioUrl());
        return "OK";
    }

    private void requireAdmin(HttpServletRequest request) {
        Object jwtIsAdminObj = request.getAttribute("jwtIsAdmin");

        if (!(jwtIsAdminObj instanceof Boolean) || !((Boolean) jwtIsAdminObj)) {
            throw new SecurityException("Admin access required.");
        }
    }
}