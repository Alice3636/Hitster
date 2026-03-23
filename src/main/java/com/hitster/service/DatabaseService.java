package com.hitster.service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.hitster.DBManager;

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