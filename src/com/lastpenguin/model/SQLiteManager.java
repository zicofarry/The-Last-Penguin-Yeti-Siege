package com.lastpenguin.model;

import java.io.File;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

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
            stmt.execute("CREATE TABLE IF NOT EXISTS players (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL);");
            // Tabel Scores
            stmt.execute("CREATE TABLE IF NOT EXISTS scores (id INTEGER PRIMARY KEY AUTOINCREMENT, player_id INTEGER NOT NULL, score INTEGER NOT NULL, yeti_killed INTEGER NOT NULL, difficulty TEXT NOT NULL, mode TEXT NOT NULL, played_at DATETIME DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (player_id) REFERENCES players(id));");
            // Tabel Settings
            stmt.execute("CREATE TABLE IF NOT EXISTS settings (id INTEGER PRIMARY KEY CHECK (id = 1), music_volume INTEGER DEFAULT 50, last_difficulty TEXT DEFAULT 'EASY', last_mode TEXT DEFAULT 'OFFLINE');");
            stmt.execute("INSERT OR IGNORE INTO settings (id, music_volume, last_difficulty, last_mode) VALUES (1, 50, 'EASY', 'OFFLINE');");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DefaultTableModel getLeaderboardData(String difficulty) {
        String[] columnNames = {"Username", "High Score", "Kills", "Missed"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        
        // Query: Ambil MAX score per username untuk difficulty tertentu
        String sql = "SELECT p.username, MAX(s.score) as top_score, s.yeti_killed, s.missed_shots " +
                    "FROM players p " +
                    "JOIN scores s ON p.id = s.player_id " +
                    "WHERE s.difficulty = ? " +
                    "GROUP BY p.username " +
                    "ORDER BY top_score DESC";

        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, difficulty);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("username"),
                    rs.getInt("top_score"),
                    rs.getInt("yeti_killed"),
                    rs.getInt("missed_shots") // Tambahkan kolom missed
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return model;
    }

    public static void saveScore(String username, int score, int killed, int missed, int bullets, String diff, String mode) {
        try (Connection conn = connect()) {
            // 1. Pastikan player ada
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

            // 3. Simpan data (Pastikan urutan ? sesuai dengan setInt)
            if (playerId != -1) {
                String sql = "INSERT INTO scores (player_id, score, missed_shots, remaining_bullets, yeti_killed, difficulty, mode) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, playerId);
                    ps.setInt(2, score);    // Kolom score
                    ps.setInt(3, missed);   // Kolom missed_shots
                    ps.setInt(4, bullets);  // Kolom remaining_bullets
                    ps.setInt(5, killed);   // Kolom yeti_killed
                    ps.setString(6, diff);
                    ps.setString(7, mode);
                    ps.executeUpdate();
                    System.out.println("Skor Berhasil Disimpan di Database!");
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