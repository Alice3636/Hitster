package com.hitster;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseLogic {

    public void playNextSong(int gameId) {
        String sql = "{CALL sp_GetNextSong(?)}";
        
        try (Connection conn = DBManager.connect();
            CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setInt(1, gameId);
            
            // מכיוון שהפרוצדורה מסתיימת ב-SELECT, נקבל ResultSet!
            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    String title = rs.getString("title");
                    String path = rs.getString("song_path");
                    System.out.println("🎵 השיר הבא למשחק: " + title + " (" + path + ")");
                } else {
                    System.out.println("🏁 אין יותר שירים פנויים למשחק זה!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    public static void main(String[] args) {

        Connection conn = DBManager.connect();

        if (conn != null) {
            String sql = "SELECT user_id, username, email, total_winnings FROM Users";

            // פתיחת משאבים (Statement ו-ResultSet) בתוך try-with-resources כדי שייסגרו
            // אוטומטית
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                    ResultSet rs = pstmt.executeQuery()) {

                System.out.println("--- 🎵 רשימת המשתמשים במערכת 🎵 ---");

                // 3. לולאת while שעוברת על כל השורות שחזרו מבסיס הנתונים
                while (rs.next()) {
                    // שליפת הנתונים מכל עמודה לפי סוג הנתון שלה
                    int id = rs.getInt("user_id");
                    String username = rs.getString("username");
                    String email = rs.getString("email");
                    double winnings = rs.getDouble("total_winnings");

                    // הדפסת השורה לקונסול
                    System.out.println("ID: " + id +
                            " | Username: " + username +
                            " | Email: " + email +
                            " | Winnings: $" + winnings);
                }

            } catch (SQLException e) {
                System.err.println("❌ שגיאה במהלך הרצת שאילתת ה-SELECT!");
                e.printStackTrace();
            }
        } else {
            System.out.println("לא ניתן להתחבר לבסיס הנתונים.");
        }
    }
}