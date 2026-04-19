package com.hitster.controller;

import com.hitster.service.DatabaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    // --- USERS ---

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        return ResponseEntity.ok(DatabaseService.getAllUsers());
    }

    @DeleteMapping("/users")
    public ResponseEntity<String> deleteUsers(@RequestBody List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return ResponseEntity.badRequest().body("No users selected for deletion.");
        }

        boolean success = DatabaseService.deleteUsers(userIds);
        if (success) {
            return ResponseEntity.ok("Users successfully deleted.");
        }
        return ResponseEntity.internalServerError().body("Failed to delete some or all users.");
    }

    // --- SONGS ---

    @GetMapping("/songs")
    public ResponseEntity<List<Map<String, Object>>> getAllSongs() {
        return ResponseEntity.ok(DatabaseService.getAllSongs());
    }

    @DeleteMapping("/songs")
    public ResponseEntity<String> deleteSongs(@RequestBody List<Integer> songIds) {
        if (songIds == null || songIds.isEmpty()) {
            return ResponseEntity.badRequest().body("No songs selected for deletion.");
        }

        boolean success = DatabaseService.deleteSongs(songIds);
        if (success) {
            return ResponseEntity.ok("Songs successfully deleted.");
        }
        return ResponseEntity.internalServerError().body("Failed to delete some or all songs.");
    }
}