package com.lastpenguin.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLManager {
    // Konfigurasi koneksi
    private static final String URL = "jdbc:mysql://localhost:3306/antarctica";
    private static final String USER = "root"; 
    private static final String PASSWORD = ""; 

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Logika: Mengambil data awal untuk memulai permainan
     * Bullets = Diambil dari record terakhir (permainan terakhir)
     * Score & Missed = Diambil dari record dengan score tertinggi (Highscore)
     * Default untuk player baru: Score 0, Missed 0, Bullets 0
     */
    public static Object[] getInitialPlayerData(String username, String difficulty) {
        // Data default: [score, missed, bullets]
        Object[] data = new Object[]{0, 0, 0}; 

        String selectPlayerSql = "SELECT id FROM players WHERE username = ?";
        
        // Query untuk ambil peluru dari sesi TERAKHIR (played_at terbaru)
        String lastSessionSql = "SELECT remaining_bullets FROM scores " +
                                "WHERE player_id = ? AND difficulty = ? " +
                                "ORDER BY played_at DESC LIMIT 1";
        
        // Query untuk ambil highscore dan missed-nya (score tertinggi)
        String highscoreSql = "SELECT score, missed_shots FROM scores " +
                              "WHERE player_id = ? AND difficulty = ? " +
                              "ORDER BY score DESC, played_at DESC LIMIT 1";

        try (Connection conn = connect()) {
            // 1. Cari ID Player berdasarkan username
            int playerId = -1;
            try (PreparedStatement ps = conn.prepareStatement(selectPlayerSql)) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) playerId = rs.getInt("id");
            }

            // 2. Jika player ditemukan di database, tarik datanya
            if (playerId != -1) {
                // Ambil Peluru Terakhir
                try (PreparedStatement ps = conn.prepareStatement(lastSessionSql)) {
                    ps.setInt(1, playerId);
                    ps.setString(2, difficulty);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        data[2] = rs.getInt("remaining_bullets");
                    }
                }

                // Ambil Highscore & Missed
                try (PreparedStatement ps = conn.prepareStatement(highscoreSql)) {
                    ps.setInt(1, playerId);
                    ps.setString(2, difficulty);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        data[0] = rs.getInt("score");
                        data[1] = rs.getInt("missed_shots");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[ONLINE] Gagal mengambil data awal: " + e.getMessage());
        }
        return data;
    }

    /**
     * Menyimpan skor ke database MySQL
     */
    public static void saveScoreOnline(String username, int score, int killed, int missed, int bullets, String diff) {
        String insertPlayerSql = "INSERT IGNORE INTO players (username) VALUES (?)";
        String selectPlayerSql = "SELECT id FROM players WHERE username = ?";
        String insertScoreSql = "INSERT INTO scores (player_id, score, missed_shots, remaining_bullets, yeti_killed, difficulty, mode) " +
                                "VALUES (?, ?, ?, ?, ?, ?, 'ONLINE')";

        try (Connection conn = connect()) {
            // Pastikan player terdaftar
            try (PreparedStatement ps = conn.prepareStatement(insertPlayerSql)) {
                ps.setString(1, username);
                ps.executeUpdate();
            }

            // Ambil ID player
            int playerId = -1;
            try (PreparedStatement ps = conn.prepareStatement(selectPlayerSql)) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) playerId = rs.getInt("id");
            }

            // Simpan Score
            if (playerId != -1) {
                try (PreparedStatement ps = conn.prepareStatement(insertScoreSql)) {
                    ps.setInt(1, playerId);
                    ps.setInt(2, score);
                    ps.setInt(3, missed);
                    ps.setInt(4, bullets);
                    ps.setInt(5, killed);
                    ps.setString(6, diff);
                    ps.executeUpdate();
                    System.out.println("[ONLINE] Skor berhasil diunggah ke server!");
                }
            }
        } catch (SQLException e) {
            System.err.println("[ONLINE] Gagal menyimpan skor: " + e.getMessage());
        }
    }

    /**
     * Mengambil Leaderboard Global
     * Menampilkan Skor Tertinggi (Highscore) tapi Peluru dari Sesi Terakhir
     */
    public static List<Object[]> getGlobalLeaderboard(String difficulty) {
        List<Object[]> data = new ArrayList<>();
        
        // Query menggunakan ROW_NUMBER() untuk mendapatkan baris terbaik dan baris terbaru secara bersamaan
        String sql = "SELECT p.username, hs.score, hs.missed_shots, ls.remaining_bullets " +
                     "FROM players p " +
                     "JOIN (" +
                     "    SELECT player_id, score, missed_shots, " +
                     "    ROW_NUMBER() OVER (PARTITION BY player_id ORDER BY score DESC, played_at DESC) as rn " +
                     "    FROM scores WHERE mode = 'ONLINE' AND difficulty = ?" +
                     ") hs ON p.id = hs.player_id AND hs.rn = 1 " +
                     "JOIN (" +
                     "    SELECT player_id, remaining_bullets, " +
                     "    ROW_NUMBER() OVER (PARTITION BY player_id ORDER BY played_at DESC) as rn " +
                     "    FROM scores WHERE mode = 'ONLINE' AND difficulty = ?" +
                     ") ls ON p.id = ls.player_id AND ls.rn = 1 " +
                     "ORDER BY hs.score DESC LIMIT 50";

        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, difficulty); 
            ps.setString(2, difficulty); 
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                data.add(new Object[]{
                    rs.getString("username"),
                    rs.getInt("score"),            
                    rs.getInt("missed_shots"),     
                    rs.getInt("remaining_bullets") 
                });
            }
        } catch (SQLException e) {
            System.err.println("[ONLINE] Gagal mengambil global leaderboard: " + e.getMessage());
        }
        return data;
    }

    /**
     * Cek koneksi server
     */
    public static boolean isServerAvailable() {
        try (Connection conn = connect()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}