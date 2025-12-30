package com.lastpenguin.model;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages local database operations using SQLite.
 * Handles the initialization of the local schema, player profiles, session records, 
 * and persistent application settings.
 */
public class SQLiteManager {
    private static final String DB_URL = "jdbc:sqlite:data/antarctica.db";
    private static final String DATA_DIR = "data";

    /**
     * Establishes a connection to the local SQLite database.
     */
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Initializes the database structure by creating required tables and directory.
     * Sets up relational tables for players and scores, and initializes default settings.
     */
    public static void initDatabase() {
        File directory = new File(DATA_DIR);
        if (!directory.exists()) directory.mkdir();

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            // Player profiles table with unique username constraint
            stmt.execute("CREATE TABLE IF NOT EXISTS players (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT UNIQUE NOT NULL);");
            
            // Relational scores table tracking session statistics and difficulty modes
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

            // Application settings table for persistent configurations
            stmt.execute("CREATE TABLE IF NOT EXISTS settings (" +
                        "id INTEGER PRIMARY KEY CHECK (id = 1), " +
                        "music_volume INTEGER DEFAULT 50, " +
                        "last_difficulty TEXT DEFAULT 'EASY', " +
                        "last_mode TEXT DEFAULT 'OFFLINE');");
            
            // Insert default configuration if it does not already exist
            stmt.execute("INSERT OR IGNORE INTO settings (id, music_volume, last_difficulty, last_mode) VALUES (1, 50, 'EASY', 'OFFLINE');");
            
        } catch (SQLException e) {
            System.err.println("Database Initialization Failed: " + e.getMessage());
        }
    }

    /**
     * Retrieves the ammunition count from the player's most recent session based on difficulty.
     * Returns 0 for new players with no existing records.
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
            System.err.println("Failed to retrieve remaining ammunition: " + e.getMessage());
        }
        return 0; 
    }

    /**
     * Aggregates data for the leaderboard display.
     * Retrieves high scores alongside session-specific statistics, ensuring the 
     * ammunition count reflects the player's very last game session.
     */
    public static List<Object[]> getLeaderboardData(String difficulty) {
        List<Object[]> data = new ArrayList<>();
        
        // Complex query utilizing joins and subqueries to synchronize high scores 
        // with ammunition data from the most recent session.
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
                     "GROUP BY p.username " + 
                     "ORDER BY high_score DESC LIMIT 50";

        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, difficulty); 
            ps.setString(2, difficulty); 
            ps.setString(3, difficulty); 
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                data.add(new Object[]{
                    rs.getString("username"),
                    rs.getInt("high_score"),
                    rs.getInt("missed_shots"), 
                    rs.getInt("last_bullets")  
                });
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve leaderboard statistics: " + e.getMessage());
        }
        return data;
    }

    /**
     * Saves the current game session results to the local database.
     * Automatically registers the player if they are a new user before inserting scores.
     */
    public static void saveScore(String username, int score, int killed, int missed, int bullets, String diff, String mode) {
        try (Connection conn = connect()) {
            // Register player if username is not yet present
            try (PreparedStatement ps = conn.prepareStatement("INSERT OR IGNORE INTO players (username) VALUES (?)")) {
                ps.setString(1, username);
                ps.executeUpdate();
            }

            // Fetch validated Player ID for relational insertion
            int playerId = -1;
            try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM players WHERE username = ?")) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) playerId = rs.getInt("id");
            }

            // Insert session record if player identification is confirmed
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
                    System.out.println("[DB] Session progress successfully saved.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Database Save Operation Failed: " + e.getMessage());
        }
    }

    /**
     * Updates persistent application settings with current configurations.
     */
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

    /**
     * Loads the stored application settings from the database.
     * Returns default settings if no record is found.
     */
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