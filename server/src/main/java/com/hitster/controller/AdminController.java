package com.hitster.controller;

import com.hitster.dto.admin.AdminUserDTO;
import com.hitster.dto.game.SongDTO;
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
    public List<SongDTO> getAllSongs(HttpServletRequest request) {
        requireAdmin(request);
        return DatabaseService.getAllSongs();
    }

    @PostMapping("/songs")
    public SongDTO createSong(@RequestBody SongDTO request, HttpServletRequest httpRequest) {
        requireAdmin(httpRequest);

        if (request == null ||
                isBlank(request.title()) ||
                isBlank(request.artist()) ||
                request.releaseYear() <= 0 ||
                isBlank(request.audioUrl())) {
            throw new IllegalArgumentException("Title, artist, releaseYear, and audioUrl are required.");
        }

        Long newId = DatabaseService.createSong(
                request.title(),
                request.artist(),
                request.releaseYear(),
                request.audioUrl()
        );

        if (newId == null) {
            throw new IllegalArgumentException("Song creation failed.");
        }

        return new SongDTO(
                newId,
                request.title(),
                request.artist(),
                request.releaseYear(),
                request.audioUrl()
        );
    }

    @PutMapping("/songs/{id}")
    public SongDTO updateSong(@PathVariable Long id,
                                   @RequestBody SongDTO request,
                                   HttpServletRequest httpRequest) {
        requireAdmin(httpRequest);

        if (request == null ||
                isBlank(request.title()) ||
                isBlank(request.artist()) ||
                request.releaseYear() <= 0 ||
                isBlank(request.audioUrl())) {
            throw new IllegalArgumentException("Title, artist, releaseYear, and audioUrl are required.");
        }

        boolean updated = DatabaseService.updateSong(
                id,
                request.title(),
                request.artist(),
                request.releaseYear(),
                request.audioUrl()
        );

        if (!updated) {
            throw new NotFoundException("Song not found: " + id);
        }

        return new SongDTO(
                id,
                request.title(),
                request.artist(),
                request.releaseYear(),
                request.audioUrl()
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