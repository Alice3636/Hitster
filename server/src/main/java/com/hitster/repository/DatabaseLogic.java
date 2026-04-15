package com.hitster.repository;

import com.hitster.service.AuthService;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.hitster.dto.MatchHistoryDTO;
import com.hitster.dto.PlayerScoreDTO;

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

    public static ObservableList<PlayerScoreDTO> getLeaderboardData() {
        int currentRank = 1;
        ObservableList<PlayerScoreDTO> leaderboard = FXCollections.observableArrayList();
    
        String query = "SELECT user_id, username, total_winnings FROM Users ORDER BY total_winnings DESC LIMIT 100"; 
        
        try (Connection conn = DBManager.connect();
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                int id = rs.getInt("user_id"); 
                String player = rs.getString("username");
                int winnings = rs.getInt("total_winnings"); 
                
                leaderboard.add(new PlayerScoreDTO(currentRank, id, player, winnings));
                currentRank++;
            }
        } catch (Exception e) {
            System.err.println("Error fetching leaderboard data: " + e.getMessage());
        }
    
        return leaderboard;
    }

    public static int getTotalWinnings(int userId) {
    int winnings = 0;
    String query = "SELECT total_winnings FROM Users WHERE user_id = ?";

    try (Connection conn = DBManager.connect();
            PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    winnings = rs.getInt("total_winnings");
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting winnings: " + e.getMessage());
        }
        return winnings;
    }

    public static String getUsername(int userId) {
    String username = "";
    String query = "SELECT username FROM Users WHERE user_id = ?";

    try (Connection conn = DBManager.connect();
            PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    username = rs.getString("username");
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting username: " + e.getMessage());
        }
        return username;
    }

    public static String getEmail(int userId) {
    String email = "";
    String query = "SELECT email FROM Users WHERE user_id = ?";

    try (Connection conn = DBManager.connect();
            PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    email = rs.getString("email");
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting email: " + e.getMessage());
        }
        return email;
    }

    public static String getWinRate(int userId) {
    Double winRate = 0.0;
    String query = "SELECT COUNT(*) AS total_games, COUNT(CASE WHEN winner_id = ? THEN 1 END) AS total_wins, IFNULL((COUNT(CASE WHEN winner_id = ? THEN 1 END) * 100.0 / NULLIF(COUNT(*), 0)), 0) AS win_rate FROM Games WHERE player1_id = ? OR player2_id = ?";
    try (Connection conn = DBManager.connect();
        PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, userId);
            pstmt.setInt(4, userId);
        
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                        winRate = rs.getDouble("win_rate");
                    }
                }
            } catch (Exception e) {
                System.err.println("Error getting email: " + e.getMessage());
            }
            return winRate+"%";
    }

    //public static String deleteMyAccount (int userId) {
    //String res = "";
    //String query = "DELETE FROM Users WHERE player_id = ?";
    //try (Connection conn = DBManager.connect();
    //    PreparedStatement pstmt = conn.prepareStatement(query)) {
    //        
    //        pstmt.setInt(1, userId);
    //    
    //        
    //        return winRate+"%";
    //}

    public static ObservableList<MatchHistoryDTO> getMatchHistory() {
        int CurrentUserId = 9;
        String result;
        ObservableList<MatchHistoryDTO> matchHistory = FXCollections.observableArrayList();
        String query = "SELECT g.player1_id, g.player2_id, g.game_date, g.winner_id, u.username AS enemy_name FROM Games g JOIN Users u ON u.user_id = CASE WHEN g.player1_id = ? THEN g.player2_id ELSE g.player1_id END WHERE g.player1_id = ? OR g.player2_id = ? ORDER BY g.game_date DESC LIMIT 100";
        
        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, CurrentUserId);
            pstmt.setInt(2, CurrentUserId);
            pstmt.setInt(3, CurrentUserId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String enemyName = rs.getString("enemy_name");
                    String date = rs.getString("game_date");
                    if (rs.getInt("winner_id") == CurrentUserId) {
                        result = "WON";
                    } else {
                        result = "LOST";
                    }
                    matchHistory.add(new MatchHistoryDTO(enemyName, date, result));
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error fetching match history data: " + e.getMessage());
        }
    
        return matchHistory;
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