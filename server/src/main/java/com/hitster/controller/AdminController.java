package com.hitster.controller;

import com.hitster.dto.AdminSongDTO;
import com.hitster.dto.AdminUserDTO;
import com.hitster.service.DatabaseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

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
            throw new NotFoundException("User not found: " + id);
        }

        return "OK";
    }

    @GetMapping("/songs")
    public List<AdminSongDTO> getAllSongs(HttpServletRequest request) {
        requireAdmin(request);
        return DatabaseService.getAllSongs();
    }

    @PostMapping("/songs")
    public AdminSongDTO createSong(@RequestBody AdminSongDTO request, HttpServletRequest httpRequest) {
        requireAdmin(httpRequest);

        if (request == null ||
                isBlank(request.getTitle()) ||
                isBlank(request.getArtist()) ||
                request.getReleaseYear() <= 0 ||
                isBlank(request.getAudioUrl())) {
            throw new IllegalArgumentException("Title, artist, releaseYear, and audioUrl are required.");
        }

        Long newId = DatabaseService.createSong(
                request.getTitle(),
                request.getArtist(),
                request.getReleaseYear(),
                request.getAudioUrl()
        );

        if (newId == null) {
            throw new IllegalArgumentException("Song creation failed.");
        }

        return new AdminSongDTO(
                newId,
                request.getTitle(),
                request.getArtist(),
                request.getReleaseYear(),
                request.getAudioUrl()
        );
    }

    @PutMapping("/songs/{id}")
    public AdminSongDTO updateSong(@PathVariable Long id,
                                   @RequestBody AdminSongDTO request,
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
                request.getTitle(),
                request.getArtist(),
                request.getReleaseYear(),
                request.getAudioUrl()
        );

        if (!updated) {
            throw new NotFoundException("Song not found: " + id);
        }

        return new AdminSongDTO(
                id,
                request.getTitle(),
                request.getArtist(),
                request.getReleaseYear(),
                request.getAudioUrl()
        );
    }

    @DeleteMapping("/songs/{id}")
    public String deleteSong(@PathVariable Long id, HttpServletRequest request) {
        requireAdmin(request);

        boolean deleted = DatabaseService.deleteSongById(id);
        if (!deleted) {
            throw new NotFoundException("Song not found: " + id);
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
}