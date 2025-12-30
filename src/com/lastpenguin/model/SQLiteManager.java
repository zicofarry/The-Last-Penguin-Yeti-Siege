package com.lastpenguin.model;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLiteManager {
    private static final String DB_URL = "jdbc:sqlite:data/antarctica.db";
    private static final String DATA_DIR = "data";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initDatabase() {
        File directory = new File(DATA_DIR);
        if (!directory.exists()) directory.mkdir();

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            // Tabel Players
            stmt.execute("CREATE TABLE IF NOT EXISTS players (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT UNIQUE NOT NULL);");
            
            // Tabel Scores
            stmt.execute("CREATE TABLE IF NOT EXISTS scores (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "player_id INTEGER NOT NULL, " +
                        "score INTEGER NOT NULL, " +
                        "yeti_killed INTEGER NOT NULL, " +
                        "missed_shots INTEGER DEFAULT 0, " +
                        "remaining_bullets INTEGER DEFAULT 0, " +
                        "difficulty TEXT NOT NULL, " +
                        "mode TEXT NOT NULL, " +
                        "played_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (player_id) REFERENCES players(id));");

            // Tabel Settings
            stmt.execute("CREATE TABLE IF NOT EXISTS settings (" +
                        "id INTEGER PRIMARY KEY CHECK (id = 1), " +
                        "music_volume INTEGER DEFAULT 50, " +
                        "last_difficulty TEXT DEFAULT 'EASY', " +
                        "last_mode TEXT DEFAULT 'OFFLINE');");
            
            stmt.execute("INSERT OR IGNORE INTO settings (id, music_volume, last_difficulty, last_mode) VALUES (1, 50, 'EASY', 'OFFLINE');");
            
        } catch (SQLException e) {
            System.err.println("Gagal Inisialisasi Database: " + e.getMessage());
        }
    }

    /**
     * MENGAMBIL PELURU TERAKHIR BERDASARKAN DIFFICULTY
     * Jika data tidak ditemukan (pemain baru), return 0.
     */
    public static int getLastBulletCount(String username, String difficulty) {
        String sql = "SELECT s.remaining_bullets FROM scores s " +
                    "JOIN players p ON s.player_id = p.id " +
                    "WHERE p.username = ? AND s.difficulty = ? " +
                    "ORDER BY s.played_at DESC LIMIT 1";

        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, difficulty);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("remaining_bullets");
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil sisa peluru: " + e.getMessage());
        }
        return 0; // Default 0 untuk pemain baru
    }

    /**
     * MENGAMBIL DATA UNTUK JTABLE
     * - Score: High Score
     * - Miss: Missed shots dari baris High Score tersebut
     * - Bullet: Sisa peluru dari baris TERAKHIR (Played At terbaru)
     */
    public static List<Object[]> getLeaderboardData(String difficulty) {
        List<Object[]> data = new ArrayList<>();
        
        // Query ini melakukan join dengan subquery MAX(score) untuk mendapatkan baris High Score,
        // dan menggunakan subquery terpisah untuk mengambil 'remaining_bullets' terbaru.
        String sql = "SELECT p.username, s.score as high_score, s.missed_shots, " +
                     "       (SELECT remaining_bullets FROM scores WHERE player_id = p.id AND difficulty = ? ORDER BY played_at DESC LIMIT 1) as last_bullets " +
                     "FROM scores s " +
                     "JOIN players p ON s.player_id = p.id " +
                     "INNER JOIN (" +
                     "    SELECT player_id, MAX(score) as max_score " +
                     "    FROM scores " +
                     "    WHERE difficulty = ? " +
                     "    GROUP BY player_id" +
                     ") m ON s.player_id = m.player_id AND s.score = m.max_score " +
                     "WHERE s.difficulty = ? " +
                     "GROUP BY p.username " + // Mengatasi jika ada skor yang sama persis
                     "ORDER BY high_score DESC LIMIT 50";

        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, difficulty); // Untuk subquery peluru terakhir
            ps.setString(2, difficulty); // Untuk subquery pencarian high score
            ps.setString(3, difficulty); // Untuk filter utama
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                data.add(new Object[]{
                    rs.getString("username"),
                    rs.getInt("high_score"),
                    rs.getInt("missed_shots"), // Diambil dari baris high score
                    rs.getInt("last_bullets")  // Diambil dari sesi terakhir
                });
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil data leaderboard: " + e.getMessage());
        }
        return data;
    }

    public static void saveScore(String username, int score, int killed, int missed, int bullets, String diff, String mode) {
        try (Connection conn = connect()) {
            // 1. Tambah player jika belum ada
            try (PreparedStatement ps = conn.prepareStatement("INSERT OR IGNORE INTO players (username) VALUES (?)")) {
                ps.setString(1, username);
                ps.executeUpdate();
            }

            // 2. Ambil ID player
            int playerId = -1;
            try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM players WHERE username = ?")) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) playerId = rs.getInt("id");
            }

            // 3. Simpan Score
            if (playerId != -1) {
                String sql = "INSERT INTO scores (player_id, score, missed_shots, remaining_bullets, yeti_killed, difficulty, mode) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, playerId);
                    ps.setInt(2, score);
                    ps.setInt(3, missed);
                    ps.setInt(4, bullets);
                    ps.setInt(5, killed);
                    ps.setString(6, diff);
                    ps.setString(7, mode);
                    ps.executeUpdate();
                    System.out.println("[DB] Progres berhasil disimpan.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Database Save Error: " + e.getMessage());
        }
    }

    public static void updateSettings(GameSettings settings) {
        try (Connection conn = connect(); 
             PreparedStatement ps = conn.prepareStatement("UPDATE settings SET last_difficulty = ?, last_mode = ?, music_volume = ? WHERE id = 1")) {
            ps.setString(1, settings.getDifficulty());
            ps.setString(2, settings.getMode());
            ps.setInt(3, settings.getMusicVolume());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static GameSettings loadSettings() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery("SELECT * FROM settings WHERE id = 1")) {
            if (rs.next()) {
                return new GameSettings(rs.getInt("music_volume"), 50, rs.getString("last_difficulty"), rs.getString("last_mode"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new GameSettings(); 
    }
}