package com.lastpenguin.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLManager {
    // Konfigurasi koneksi (Localhost untuk sekarang)
    private static final String URL = "jdbc:mysql://localhost:3306/antarctica";
    private static final String USER = "root"; // Sesuaikan dengan user MySQL kamu
    private static final String PASSWORD = ""; // Sesuaikan dengan password MySQL kamu

    /**
     * Membuka koneksi ke MySQL Server
     */
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Menyimpan skor ke database Online (MySQL)
     */
    public static void saveScoreOnline(String username, int score, int killed, int missed, int bullets, String diff) {
        String insertPlayerSql = "INSERT IGNORE INTO players (username) VALUES (?)";
        String selectPlayerSql = "SELECT id FROM players WHERE username = ?";
        String insertScoreSql = "INSERT INTO scores (player_id, score, missed_shots, remaining_bullets, yeti_killed, difficulty, mode) " +
                                "VALUES (?, ?, ?, ?, ?, ?, 'ONLINE')";

        try (Connection conn = connect()) {
            // 1. Pastikan player terdaftar
            try (PreparedStatement ps = conn.prepareStatement(insertPlayerSql)) {
                ps.setString(1, username);
                ps.executeUpdate();
            }

            // 2. Ambil ID player
            int playerId = -1;
            try (PreparedStatement ps = conn.prepareStatement(selectPlayerSql)) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) playerId = rs.getInt("id");
            }

            // 3. Simpan Score jika player_id valid
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
            System.err.println("[ONLINE] Gagal menyimpan skor ke MySQL: " + e.getMessage());
        }
    }

    /**
     * Mengambil data Leaderboard Global dari MySQL
     */
    public static List<Object[]> getGlobalLeaderboard(String difficulty) {
        List<Object[]> data = new ArrayList<>();
        
        // Query diperbaiki agar kompatibel dengan sql_mode=only_full_group_by
        // Menggunakan fungsi MAX untuk kolom detail agar memenuhi aturan grouping MySQL
        String sql = "SELECT p.username, " +
                    "       MAX(s.score) as high_score, " +
                    "       MAX(s.missed_shots) as missed_shots, " +
                    "       MAX(s.remaining_bullets) as remaining_bullets " +
                    "FROM players p " +
                    "JOIN scores s ON p.id = s.player_id " +
                    "WHERE s.difficulty = ? AND s.mode = 'ONLINE' " +
                    "GROUP BY p.username " +
                    "ORDER BY high_score DESC LIMIT 50";

        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, difficulty);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                data.add(new Object[]{
                    rs.getString("username"),
                    rs.getInt("high_score"),
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
     * Cek apakah koneksi ke server tersedia
     */
    public static boolean isServerAvailable() {
        try (Connection conn = connect()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
