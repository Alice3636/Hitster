package com.hitster.service;

import com.hitster.dto.admin.UserEntryDTO;
import com.hitster.dto.game.SongDTO;
import com.hitster.dto.user.LeaderboardResponseDTO;
import com.hitster.dto.user.LeaderboardEntryDTO;
import com.hitster.dto.user.MatchHistoryDTO;
import com.hitster.dto.user.UpdateProfileRequestDTO;
import com.hitster.dto.user.UserProfileResponseDTO;
import com.hitster.repository.DBManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {

    static Connection conn = DBManager.connect();

    public static int registerUser(String username, String email, String passwordHash, String picturePath) {
        String sql = "{CALL sp_RegisterUser(?, ?, ?, ?, ?)}";

        try (CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setString(1, username);
            cstmt.setString(2, email);
            cstmt.setString(3, passwordHash);
            cstmt.setString(4, picturePath);

            cstmt.registerOutParameter(5, Types.INTEGER);
            cstmt.execute();

            return cstmt.getInt(5);

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static boolean usernameExists(String username) {
        String sql = "SELECT * FROM Users WHERE username = ? LIMIT 1";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean emailExists(String email) {
        String sql = "SELECT * FROM Users WHERE email = ? LIMIT 1";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean usernameExistsForOtherUser(Long userId, String username) {
        String sql = "SELECT * FROM Users WHERE username = ? AND user_id <> ? LIMIT 1";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setLong(2, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean emailExistsForOtherUser(Long userId, String email) {
        String sql = "SELECT * FROM Users WHERE email = ? AND user_id <> ? LIMIT 1";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setLong(2, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getUserPasswordHash(String email) {
        String sql = "SELECT password_hash FROM Users WHERE email = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password_hash");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Long getUserIdByEmail(String email) {
        String sql = "SELECT user_id FROM Users WHERE email = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("user_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getUsernameByEmail(String email) {
        String sql = "SELECT username FROM Users WHERE email = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isAdminByEmail(String email) {
        String sql = "SELECT is_admin FROM Users WHERE email = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("is_admin");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static List<UserEntryDTO> getAllUsers() {
        String sql = "SELECT user_id, username, email, is_admin FROM Users ORDER BY user_id";
        List<UserEntryDTO> users = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                users.add(new UserEntryDTO(
                        rs.getLong("user_id"),
                        rs.getString("username"),
                        rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    public static boolean deleteUserById(Long userId) {
        String sql = "DELETE FROM Users WHERE user_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static UserProfileResponseDTO getUserMeById(Long userId) {
        String sql = "SELECT user_id, username, email, is_admin, total_winnings, profile_picture_path " +
                "FROM Users WHERE user_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int totalWins = rs.getObject("total_winnings") != null
                            ? rs.getInt("total_winnings")
                            : 0;

                    return new UserProfileResponseDTO(
                            rs.getString("username"),
                            rs.getString("email"),
                            totalWins,
                            calculateWinRate(userId),
                            getMatchHistoryForUser(userId),
                            rs.getString("profile_picture_path")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static List<MatchHistoryDTO> getMatchHistoryForUser(Long userId) {
        String sql = """
                SELECT 
                    g.game_date,
                    g.winner_id,
                    CASE
                        WHEN g.player1_id = ? THEN u2.username
                        ELSE u1.username
                    END AS opponent_username
                FROM Games g
                JOIN Users u1 ON g.player1_id = u1.user_id
                JOIN Users u2 ON g.player2_id = u2.user_id
                WHERE g.player1_id = ? OR g.player2_id = ?
                ORDER BY g.game_date DESC
                """;

        List<MatchHistoryDTO> history = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setLong(2, userId);
            pstmt.setLong(3, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Long winnerId = rs.getObject("winner_id") != null
                            ? rs.getLong("winner_id")
                            : null;

                    String result;
                    if (winnerId == null) {
                        result = "Draw";
                    } else if (winnerId.equals(userId)) {
                        result = "Win";
                    } else {
                        result = "Lose";
                    }

                    Timestamp gameDate = rs.getTimestamp("game_date");

                    history.add(new MatchHistoryDTO(
                            rs.getString("opponent_username"),
                            gameDate != null ? gameDate.toString() : "",
                            result
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return history;
    }

    private static double calculateWinRate(Long userId) {
        String sql = """
                SELECT
                    COUNT(*) AS total_games,
                    SUM(CASE WHEN winner_id = ? THEN 1 ELSE 0 END) AS wins
                FROM Games
                WHERE player1_id = ? OR player2_id = ?
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setLong(2, userId);
            pstmt.setLong(3, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int totalGames = rs.getInt("total_games");
                    int wins = rs.getInt("wins");

                    if (totalGames == 0) {
                        return 0.0;
                    }

                    return (wins * 100.0) / totalGames;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    public static boolean updateCurrentUser(Long userId, UpdateProfileRequestDTO request) {
        String sql = "UPDATE Users SET username = ?, email = ?, profile_picture_path = ? WHERE user_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, request.username());
            pstmt.setString(2, request.email());
            pstmt.setString(3, request.profilePicturePath());
            pstmt.setLong(4, userId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static LeaderboardResponseDTO getLeaderboard() {
        String sql = "SELECT user_id, username, total_winnings, profile_picture_path " +
                "FROM Users ORDER BY total_winnings DESC, user_id ASC";
        List<LeaderboardEntryDTO> entries = new ArrayList<>();
        int rank = 1;

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int winnings = rs.getObject("total_winnings") != null
                        ? rs.getInt("total_winnings")
                        : 0;

                entries.add(new LeaderboardEntryDTO(
                        rank++,
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        winnings
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new LeaderboardResponseDTO(entries);
    }

    public static void recordGameResult(Long player1Id, Long player2Id, Long winnerId, int player1Score, int player2Score) {
        String insertSql = """
                INSERT INTO Games
                (player1_id, player2_id, player1_score, player2_score, current_turn, status, winner_id, game_date)
                VALUES (?, ?, ?, ?, NULL, 'finished', ?, NOW())
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setLong(1, player1Id);
            pstmt.setLong(2, player2Id);
            pstmt.setInt(3, player1Score);
            pstmt.setInt(4, player2Score);

            if (winnerId == null) {
                pstmt.setNull(5, Types.INTEGER);
            } else {
                pstmt.setLong(5, winnerId);
            }

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        if (winnerId != null) {
            String updateSql = "UPDATE Users SET total_winnings = COALESCE(total_winnings, 0) + 1 WHERE user_id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setLong(1, winnerId);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<SongDTO> getAllSongs() {
        String sql = "SELECT song_id, title, artist, release_year, song_path FROM Songs ORDER BY song_id";
        List<SongDTO> songs = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                songs.add(new SongDTO(
                        rs.getLong("song_id"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getInt("release_year"),
                        rs.getString("song_path")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return songs;
    }

    public static Long createSong(String title, String artist, int releaseYear, String audioUrl) {
        String sql = "INSERT INTO Songs (title, artist, release_year, song_path, cover_path) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, title);
            pstmt.setString(2, artist);
            pstmt.setInt(3, releaseYear);
            pstmt.setString(4, audioUrl);
            pstmt.setString(5, null);

            int affected = pstmt.executeUpdate();
            if (affected == 0) {
                return null;
            }

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean updateSong(Long songId, String title, String artist, int releaseYear, String audioUrl) {
        String sql = "UPDATE Songs SET title = ?, artist = ?, release_year = ?, song_path = ? WHERE song_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, artist);
            pstmt.setInt(3, releaseYear);
            pstmt.setString(4, audioUrl);
            pstmt.setLong(5, songId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteSongById(Long songId) {
        String sql = "DELETE FROM Songs WHERE song_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, songId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void updateUserPassword(String email, String hashedPassword) {
        String query = "UPDATE Users SET password_hash = ? WHERE email = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, hashedPassword);
            pstmt.setString(2, email);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error while updating password for " + email + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}