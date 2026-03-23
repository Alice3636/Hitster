package com.hitster;

import com.hitster.service.AuthService;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.hitster.model.PlayerScore;

public class DatabaseLogic {

    public static boolean isUserAdmin(String username) {
        String sql = "SELECT is_admin FROM Users WHERE username = ?";
        
        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("is_admin");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error executing admin check");
            e.printStackTrace();
        }
        return false;
    }

    public void playNextSong(int gameId) {
        String sql = "{CALL sp_GetNextSong(?)}";
        
        try (Connection conn = DBManager.connect();
            CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setInt(1, gameId);
            
            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    String title = rs.getString("title");
                    String path = rs.getString("song_path");
                    System.out.println("Next song for game: " + title + " (" + path + ")");
                } else {
                    System.out.println("No more songs available for this game.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ObservableList<PlayerScore> getLeaderboardData() {
        int currentRank = 1;
        ObservableList<PlayerScore> leaderboard = FXCollections.observableArrayList();
    
        String query = "SELECT user_id, username, total_winnings FROM Users ORDER BY total_winnings DESC LIMIT 100"; 
        
        try (Connection conn = DBManager.connect();
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                int id = rs.getInt("user_id"); 
                String player = rs.getString("username");
                int winnings = rs.getInt("total_winnings"); 
                
                leaderboard.add(new PlayerScore(currentRank, id, player, winnings));
                currentRank++;
            }
        } catch (Exception e) {
            System.err.println("Error fetching leaderboard data: " + e.getMessage());
        }
    
        return leaderboard;
    }


    public static void main(String[] args) {
        System.out.println("Starting test user creation...");

        AuthService.register("alice", "alice@test.com", "123456");
        AuthService.register("maayan", "maayan@test.com", "123456");
        System.out.println("Users registered.");

        String updateSql = "UPDATE Users SET is_admin = true WHERE username = 'alice'";
        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.executeUpdate();
            System.out.println("Admin permissions granted to alice.");
        } catch (SQLException e) {
            System.out.println("Error updating permissions:");
            e.printStackTrace();
        }

        Connection conn = DBManager.connect();
        if (conn != null) {
            String sql = "SELECT user_id, username, email, is_admin FROM Users";

            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                System.out.println("--- System Users ---");

                while (rs.next()) {
                    int id = rs.getInt("user_id");
                    String username = rs.getString("username");
                    String email = rs.getString("email");
                    boolean isAdmin = rs.getBoolean("is_admin");

                    System.out.println("ID: " + id +
                            " | Username: " + username +
                            " | Email: " + email +
                            " | Admin? " + isAdmin);
                }

            } catch (SQLException e) {
                System.err.println("Error executing SELECT query.");
                e.printStackTrace();
            }
        }
    }
}