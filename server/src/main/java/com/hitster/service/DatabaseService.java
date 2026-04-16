package com.hitster.service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import com.hitster.model.Song;
import com.hitster.repository.DBManager;

public class DatabaseService {

    // Single shared DB connection
    static Connection conn = DBManager.connect();

    /**
     * Registers a new user in the system
     *
     * @return new user ID, or -1 if something failed
     */
    public static int registerUser(String username, String email, String passwordHash, String picturePath) {
        String sql = "{CALL sp_RegisterUser(?, ?, ?, ?)}";

        try (CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setString(1, username);
            cstmt.setString(2, email);
            cstmt.setString(3, passwordHash);
            cstmt.setString(4, picturePath);
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
        // Updated to match the new 3-parameter signature
        String sql = "{CALL sp_GetUser(NULL, NULL, ?)}";

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
     * Fetches a list of random songs to build the game deck.
     */
    public static List<Song> getRandomSongs(int limit) {
        List<Song> songs = new java.util.ArrayList<>();
        String sql = "{CALL sp_GetRandomSongs(?)}";

        try (java.sql.Connection conn = DBManager.connect();
                java.sql.CallableStatement cstmt = conn.prepareCall(sql)) {

            cstmt.setInt(1, limit);

            try (java.sql.ResultSet rs = cstmt.executeQuery()) {
                while (rs.next()) {
                    String id = String.valueOf(rs.getInt("song_id"));
                    String title = rs.getString("title");
                    String artist = rs.getString("artist");
                    int year = rs.getInt("release_year");
                    String path = rs.getString("song_path");

                    // Adds the real song from DB to the Java deck
                    songs.add(new com.hitster.model.Song(id, title, artist, year, path));
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error fetching random songs: " + e.getMessage());
        }
        return songs;
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

    /**
     * Updates a user's password hash (Used for Forgot Password)
     */
    public static boolean updateUserPassword(String email, String newPasswordHash) {
        String sql = "{CALL sp_UpdatePassword(?, ?)}";

        try (Connection conn = DBManager.connect();
                CallableStatement cstmt = conn.prepareCall(sql)) {

            cstmt.setString(1, email);
            cstmt.setString(2, newPasswordHash);

            int rowsAffected = cstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating password:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks the database to see if the provided API token is valid.
     * Returns true if the token exists and is assigned to a user.
     */
    public static int getUserIdByToken(String token) {
        String sql = "{CALL sp_VerifyToken(?)}";

        try (Connection conn = DBManager.connect();
                CallableStatement cstmt = conn.prepareCall(sql)) {

            cstmt.setString(1, token);

            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id"); // Token found! Return the ID.
                }
            }

        } catch (SQLException e) {
            System.err.println("Error verifying token: " + e.getMessage());
        }
        return -1; // Token is fake or expired
    }
}