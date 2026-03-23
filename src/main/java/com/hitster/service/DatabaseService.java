package com.hitster.service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hitster.DBManager;

public class DatabaseService {

    public static void main(String[] args) {

        // Simple integration test for the main DB flow
        System.out.println("=== Starting integration test ===");

        // --- 1. Register two users ---
        System.out.println("\n[1] Registering test players...");

        // Use timestamp so emails are unique on every run
        long timestamp = System.currentTimeMillis();

        int player1Id = DatabaseService.registerUser(
                "GamerA_" + timestamp,
                "a" + timestamp + "@test.com",
                "hash_a");

        int player2Id = DatabaseService.registerUser(
                "GamerB_" + timestamp,
                "b" + timestamp + "@test.com",
                "hash_b");

        if (player1Id > 0 && player2Id > 0) {
            System.out.println("Players created successfully. IDs: " + player1Id + ", " + player2Id);
        } else {
            System.err.println("Failed to create players. Stopping test.");
            return;
        }

        // --- 2. Login data check ---
        System.out.println("\n[2] Checking login data...");

        // Retrieve stored password hash
        String fetchedHash = DatabaseService.getUserPasswordHash("a" + timestamp + "@test.com");

        // Basic validation
        if ("hash_a".equals(fetchedHash)) {
            System.out.println("Login data looks correct.");
        } else {
            System.err.println("Password hash mismatch.");
        }

        // --- 3. Create a new game ---
        System.out.println("\n[3] Creating new game with 5 songs...");

        int gameId = DatabaseService.startNewGame(player1Id, player2Id, 5);

        if (gameId > 0) {
            System.out.println("Game created. ID: " + gameId);
        } else {
            System.err.println("Game creation failed. Stopping test.");
            return;
        }

        // --- 4. Simulate a few turns ---
        System.out.println("\n[4] Simulating gameplay...");

        // Turn 1 - player 1
        String song1 = DatabaseService.getNextSong(gameId);
        System.out.println("Song for turn 1: " + (song1 != null ? song1 : "No songs available"));

        // Assume player 1 guessed correctly and got 10 points
        boolean scoreUpdated = DatabaseService.updateScore(gameId, player1Id, 10, player2Id);
        System.out.println("Score updated for player 1 (10 pts). Turn switched? " + scoreUpdated);

        // Turn 2 - player 2
        String song2 = DatabaseService.getNextSong(gameId);
        System.out.println("Song for turn 2: " + (song2 != null ? song2 : "No songs available"));

        // Assume player 2 failed (0 points)
        DatabaseService.updateScore(gameId, player2Id, 0, player1Id);
        System.out.println("Score updated for player 2 (0 pts). Turn back to player 1.");

        // --- 5. End the game ---
        System.out.println("\n[5] Ending the game...");

        // Player 1 wins and gets reward
        boolean gameEnded = DatabaseService.endGame(gameId, player1Id, 50.0);

        if (gameEnded) {
            System.out.println("Game finished. Player " + player1Id + " declared winner.");
        } else {
            System.err.println("Error while closing the game.");
        }

        System.out.println("\n=== Test completed ===");
    }

    // Single shared DB connection
    static Connection conn = DBManager.connect();

    /**
     * Registers a new user in the system
     *
     * @return new user ID, or -1 if something failed
     */
    public static int registerUser(String username, String email, String passwordHash) {
        String sql = "{CALL sp_RegisterUser(?, ?, ?, ?)}";

        try (CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setString(1, username);
            cstmt.setString(2, email);
            cstmt.setString(3, passwordHash);
            cstmt.registerOutParameter(4, Types.INTEGER);

            cstmt.execute();
            return cstmt.getInt(4);

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Retrieves the stored password hash for login validation
     *
     * @return password hash from DB or null if user not found
     */
    public static String getUserPasswordHash(String email) {
        String sql = "{CALL sp_GetUser(?)}";

        try (CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setString(1, email);

            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password_hash");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving login data:");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Starts a new game and randomly assigns songs
     *
     * @return game ID or -1 if creation failed
     */
    public static int startNewGame(int player1Id, int player2Id, int numSongs) {
        String sql = "{CALL sp_StartNewGame(?, ?, ?, ?)}";

        try (CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, player1Id);
            cstmt.setInt(2, player2Id);
            cstmt.setInt(3, numSongs);
            cstmt.registerOutParameter(4, Types.INTEGER);

            cstmt.execute();
            return cstmt.getInt(4);

        } catch (SQLException e) {
            System.err.println("Error creating new game:");
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Returns the next song that hasn't been played yet
     *
     * @return formatted song info or null if no songs left
     */
    public static String getNextSong(int gameId) {
        String sql = "{CALL sp_GetNextSong(?)}";

        try (CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, gameId);

            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    String title = rs.getString("title");
                    String artist = rs.getString("artist");
                    String path = rs.getString("song_path");

                    return title + " by " + artist + " [" + path + "]";
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving next song:");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Updates player score and switches the turn
     */
    public static boolean updateScore(int gameId, int playerId, int pointsToAdd, int nextTurnPlayerId) {
        String sql = "{CALL sp_UpdateScore(?, ?, ?, ?)}";

        try (CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, gameId);
            cstmt.setInt(2, playerId);
            cstmt.setInt(3, pointsToAdd);
            cstmt.setInt(4, nextTurnPlayerId);

            int rowsAffected = cstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating score:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Ends the game and updates the winner's earnings
     */
    public static boolean endGame(int gameId, int winnerId, double winningsAmount) {
        String sql = "{CALL sp_EndGame(?, ?, ?)}";

        try (CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, gameId);
            cstmt.setInt(2, winnerId);
            cstmt.setDouble(3, winningsAmount);

            int rowsAffected = cstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error ending the game:");
            e.printStackTrace();
            return false;
        }
    }
}