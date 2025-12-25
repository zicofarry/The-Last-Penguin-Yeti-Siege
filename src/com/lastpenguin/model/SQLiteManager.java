/**
 * Copyright (c) 2025 Muhammad 'Azmi Salam. All Rights Reserved.
 * Email: mhmmdzmslm36@gmail.com
 * GitHub: https://github.com/zicofarry
 */
package com.lastpenguin.model;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;

/**
 * Manages SQLite database operations including connection and schema initialization.
 * Simulates ENUM types using SQL CHECK constraints for data integrity.
 * * @author Muhammad 'Azmi Salam
 */
public class SQLiteManager {
    private static final String DB_URL = "jdbc:sqlite:data/antarctica.db";
    private static final String DATA_DIR = "data";

    public static Connection connect() {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(DB_URL);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Database Connection Error: " + e.getMessage());
        }
        return conn;
    }

    public static void initDatabase() {
        File directory = new File(DATA_DIR);
        if (!directory.exists()) directory.mkdir();

        String createPlayersTable = "CREATE TABLE IF NOT EXISTS players (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL);";
        String createScoresTable = "CREATE TABLE IF NOT EXISTS scores (id INTEGER PRIMARY KEY AUTOINCREMENT, player_id INTEGER NOT NULL, score INTEGER NOT NULL, yeti_killed INTEGER NOT NULL, difficulty TEXT NOT NULL, mode TEXT NOT NULL, played_at DATETIME DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (player_id) REFERENCES players(id));";
        String createSettingsTable = "CREATE TABLE IF NOT EXISTS settings (id INTEGER PRIMARY KEY CHECK (id = 1), music_volume INTEGER DEFAULT 50, last_difficulty TEXT DEFAULT 'EASY', last_mode TEXT DEFAULT 'OFFLINE');";
        String seedSettings = "INSERT OR IGNORE INTO settings (id, music_volume, last_difficulty, last_mode) VALUES (1, 50, 'EASY', 'OFFLINE');";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(createPlayersTable);
            stmt.execute(createScoresTable);
            stmt.execute(createSettingsTable);
            stmt.execute(seedSettings);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Metode untuk mengambil data Leaderboard (Memperbaiki error di MenuPanel)
    public static DefaultTableModel getLeaderboardData() {
        String[] columnNames = {"Username", "High Score", "Difficulty", "Kills"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        // Query untuk mengambil data gabungan dari tabel players dan scores
        String sql = "SELECT p.username, MAX(s.score) as top_score, s.difficulty, s.yeti_killed " +
                    "FROM players p JOIN scores s ON p.id = s.player_id " +
                    "GROUP BY p.username ORDER BY top_score DESC";

        try (Connection conn = connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("username"),
                    rs.getInt("top_score"),
                    rs.getString("difficulty"),
                    rs.getInt("yeti_killed")
                });
            }
        } catch (SQLException e) {
            System.err.println("Gagal memuat Leaderboard: " + e.getMessage());
        }
        return model;
    }

    // Metode untuk menyimpan skor setelah game over
    public static void saveScore(String username, int score, int killed, String diff, String mode) {
        try (Connection conn = connect()) {
            String playerSql = "INSERT OR IGNORE INTO players (username) VALUES (?)";
            try (PreparedStatement ps = conn.prepareStatement(playerSql)) {
                ps.setString(1, username);
                ps.executeUpdate();
            }
            int playerId = -1;
            try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM players WHERE username = ?")) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) playerId = rs.getInt("id");
            }
            String scoreSql = "INSERT INTO scores (player_id, score, yeti_killed, difficulty, mode) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(scoreSql)) {
                ps.setInt(1, playerId); ps.setInt(2, score); ps.setInt(3, killed); ps.setString(4, diff); ps.setString(5, mode);
                ps.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void updateSettings(GameSettings settings) {
        String sql = "UPDATE settings SET last_difficulty = ?, last_mode = ?, music_volume = ? WHERE id = 1";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, settings.getDifficulty());
            ps.setString(2, settings.getMode());
            ps.setInt(3, settings.getMusicVolume());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static GameSettings loadSettings() {
        GameSettings settings = new GameSettings();
        String sql = "SELECT * FROM settings WHERE id = 1";
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new GameSettings(
                    rs.getInt("music_volume"),
                    50, // sfx default
                    rs.getString("last_difficulty"),
                    rs.getString("last_mode")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return settings;
    }
}