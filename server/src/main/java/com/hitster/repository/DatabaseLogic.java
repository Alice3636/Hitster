package com.hitster.repository;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.hitster.model.MatchHistoryObj;
import com.hitster.model.PlayerScore;

public class DatabaseLogic {

    public static boolean isUserAdmin(String username) {
        String sql = "{CALL sp_GetUser(NULL, ?, NULL)}";

        try (Connection conn = DBManager.connect();
                CallableStatement cstmt = conn.prepareCall(sql)) {

            cstmt.setString(1, username);

            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("is_admin");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error executing admin check: " + e.getMessage());
        }
        return false;
    }

    // FIX 1: Swapped ObservableList for standard java.util.List
    public static List<PlayerScore> getLeaderboardData() {
        int currentRank = 1;
        List<PlayerScore> leaderboard = new ArrayList<>();
        String sql = "{CALL sp_GetLeaderboard()}";

        try (Connection conn = DBManager.connect();
                CallableStatement cstmt = conn.prepareCall(sql);
                ResultSet rs = cstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("user_id");
                String player = rs.getString("username");
                int winnings = rs.getInt("total_winnings");

                leaderboard.add(new PlayerScore(currentRank, id, player, winnings));
                currentRank++;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching leaderboard data: " + e.getMessage());
        }

        return leaderboard;
    }

    private static Object getUserFieldById(int userId, String columnName) {
        String sql = "{CALL sp_GetUser(?, NULL, NULL)}";
        try (Connection conn = DBManager.connect();
                CallableStatement cstmt = conn.prepareCall(sql)) {

            cstmt.setInt(1, userId);

            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject(columnName);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting " + columnName + ": " + e.getMessage());
        }
        return null;
    }

    public static int getTotalWinnings(int userId) {
        Object result = getUserFieldById(userId, "total_winnings");
        return result != null ? (Integer) result : 0;
    }

    public static String getUsername(int userId) {
        Object result = getUserFieldById(userId, "username");
        return result != null ? (String) result : "";
    }

    public static String getEmail(int userId) {
        Object result = getUserFieldById(userId, "email");
        return result != null ? (String) result : "";
    }

    public static String getWinRate(int userId) {
        double winRate = 0.0;
        String sql = "{CALL sp_GetUserWinRate(?)}";

        try (Connection conn = DBManager.connect();
                CallableStatement cstmt = conn.prepareCall(sql)) {

            cstmt.setInt(1, userId);

            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    winRate = rs.getDouble("win_rate");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting win rate: " + e.getMessage());
        }
        return winRate + "%";
    }

    // FIX 2: Swapped ObservableList for standard java.util.List
    public static List<MatchHistoryObj> getMatchHistory(int userId) {
        List<MatchHistoryObj> matchHistory = new ArrayList<>();
        String sql = "{CALL sp_GetMatchHistory(?)}";

        try (Connection conn = DBManager.connect();
                CallableStatement cstmt = conn.prepareCall(sql)) {

            cstmt.setInt(1, userId);

            try (ResultSet rs = cstmt.executeQuery()) {
                while (rs.next()) {
                    String enemyName = rs.getString("enemy_name");
                    String date = rs.getString("game_date");
                    String result = (rs.getInt("winner_id") == userId) ? "WON" : "LOST";

                    matchHistory.add(new MatchHistoryObj(enemyName, date, result));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching match history data: " + e.getMessage());
        }

        return matchHistory;
    }
}