package com.hitster.service;

import com.hitster.dto.AdminSongDTO;
import com.hitster.dto.AdminUserDTO;
import com.hitster.dto.LeaderboardDTO;
import com.hitster.dto.LeaderboardEntryDTO;
import com.hitster.dto.UpdateMeRequestDTO;
import com.hitster.dto.UserMeDTO;
import com.hitster.repository.DBManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {

    static Connection conn = DBManager.connect();

    // ================= USERS =================

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

    public static boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM Users WHERE username = ? LIMIT 1";

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
        String sql = "SELECT 1 FROM Users WHERE email = ? LIMIT 1";

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
        String sql = "SELECT 1 FROM Users WHERE username = ? AND user_id <> ? LIMIT 1";

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
        String sql = "SELECT 1 FROM Users WHERE email = ? AND user_id <> ? LIMIT 1";

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

    public static List<AdminUserDTO> getAllUsers() {
        String sql = "SELECT user_id, username, email, is_admin FROM Users ORDER BY user_id";
        List<AdminUserDTO> users = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                users.add(new AdminUserDTO(
                        rs.getLong("user_id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getBoolean("is_admin")
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

    public static UserMeDTO getUserMeById(Long userId) {
        String sql = "SELECT user_id, username, email, is_admin, total_winnings, profile_picture_path " +
                "FROM Users WHERE user_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new UserMeDTO(
                            rs.getLong("user_id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getBoolean("is_admin"),
                            rs.getObject("total_winnings") != null ? rs.getInt("total_winnings") : null,
                            rs.getString("profile_picture_path")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean updateCurrentUser(Long userId, UpdateMeRequestDTO request) {
        String sql = "UPDATE Users SET username = ?, email = ?, profile_picture_path = ? WHERE user_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, request.getUsername());
            pstmt.setString(2, request.getEmail());
            pstmt.setString(3, request.getProfilePicturePath());
            pstmt.setLong(4, userId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static LeaderboardDTO getLeaderboard() {
        String sql = "SELECT user_id, username, total_winnings, profile_picture_path " +
                "FROM Users ORDER BY total_winnings DESC, user_id ASC";
        List<LeaderboardEntryDTO> entries = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Integer winnings = rs.getObject("total_winnings") != null ? rs.getInt("total_winnings") : 0;

                entries.add(new LeaderboardEntryDTO(
                        rs.getLong("user_id"),
                        rs.getString("username"),
                        winnings,
                        rs.getString("profile_picture_path")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new LeaderboardDTO(entries);
    }

    // ================= SONGS =================

    public static List<AdminSongDTO> getAllSongs() {
        String sql = "SELECT song_id, title, artist, release_year, song_path FROM Songs ORDER BY song_id";
        List<AdminSongDTO> songs = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                songs.add(new AdminSongDTO(
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
}