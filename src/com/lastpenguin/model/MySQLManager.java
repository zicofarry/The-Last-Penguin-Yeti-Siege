package com.lastpenguin.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages remote database interactions using MySQL.
 * Facilitates player data synchronization, global high-score tracking, 
 * and server availability checks for the Online game mode.
 */
public class MySQLManager {
    // Database connection configurations
    private static final String URL = "jdbc:mysql://localhost:3306/antarctica";
    private static final String USER = "root"; 
    private static final String PASSWORD = ""; 

    /**
     * Establishes a connection to the MySQL server.
     */
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Retrieves the initial player state to begin a game session.
     * Ammunition is fetched from the last recorded session, while score 
     * and missed shots are retrieved from the historical high-score record.
     */
    public static Object[] getInitialPlayerData(String username, String difficulty) {
        // Default values: [score, missed, bullets]
        Object[] data = new Object[]{0, 0, 0}; 

        String selectPlayerSql = "SELECT id FROM players WHERE username = ?";
        
        // Query to retrieve ammunition from the most recent session
        String lastSessionSql = "SELECT remaining_bullets FROM scores " +
                                "WHERE player_id = ? AND difficulty = ? " +
                                "ORDER BY played_at DESC LIMIT 1";
        
        // Query to retrieve the highest score and its associated missed shots
        String highscoreSql = "SELECT score, missed_shots FROM scores " +
                              "WHERE player_id = ? AND difficulty = ? " +
                              "ORDER BY score DESC, played_at DESC LIMIT 1";

        try (Connection conn = connect()) {
            // 1. Identify the Player ID based on the provided username
            int playerId = -1;
            try (PreparedStatement ps = conn.prepareStatement(selectPlayerSql)) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) playerId = rs.getInt("id");
            }

            // 2. If the player exists, populate the data array from existing records
            if (playerId != -1) {
                // Fetch ammunition count from the last session
                try (PreparedStatement ps = conn.prepareStatement(lastSessionSql)) {
                    ps.setInt(1, playerId);
                    ps.setString(2, difficulty);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        data[2] = rs.getInt("remaining_bullets");
                    }
                }

                // Fetch high-score and corresponding missed shot statistics
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
            System.err.println("[ONLINE] Failed to retrieve initial player data: " + e.getMessage());
        }
        return data;
    }

    /**
     * Persists the current game session results to the remote MySQL database.
     * Ensures the player is registered in the database before inserting session records.
     */
    public static void saveScoreOnline(String username, int score, int killed, int missed, int bullets, String diff) {
        String insertPlayerSql = "INSERT IGNORE INTO players (username) VALUES (?)";
        String selectPlayerSql = "SELECT id FROM players WHERE username = ?";
        String insertScoreSql = "INSERT INTO scores (player_id, score, missed_shots, remaining_bullets, yeti_killed, difficulty, mode) " +
                                "VALUES (?, ?, ?, ?, ?, ?, 'ONLINE')";

        try (Connection conn = connect()) {
            // Ensure the player is registered in the system
            try (PreparedStatement ps = conn.prepareStatement(insertPlayerSql)) {
                ps.setString(1, username);
                ps.executeUpdate();
            }

            // Retrieve the verified Player ID
            int playerId = -1;
            try (PreparedStatement ps = conn.prepareStatement(selectPlayerSql)) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) playerId = rs.getInt("id");
            }

            // Execute the score insertion if player identity is confirmed
            if (playerId != -1) {
                try (PreparedStatement ps = conn.prepareStatement(insertScoreSql)) {
                    ps.setInt(1, playerId);
                    ps.setInt(2, score);
                    ps.setInt(3, missed);
                    ps.setInt(4, bullets);
                    ps.setInt(5, killed);
                    ps.setString(6, diff);
                    ps.executeUpdate();
                    System.out.println("[ONLINE] Score successfully uploaded to the server.");
                }
            }
        } catch (SQLException e) {
            System.err.println("[ONLINE] Failed to save session score: " + e.getMessage());
        }
    }

    /**
     * Retrieves the Global Leaderboard entries.
     * Aggregates data to display the highest historical score alongside the 
     * ammunition count from the player's most recent session using window functions.
     */
    public static List<Object[]> getGlobalLeaderboard(String difficulty) {
        List<Object[]> data = new ArrayList<>();
        
        // Complex query utilizing ROW_NUMBER() to synchronize high-scores and latest session data
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
            System.err.println("[ONLINE] Failed to retrieve global leaderboard: " + e.getMessage());
        }
        return data;
    }

    /**
     * Validates if the remote database server is accessible.
     */
    public static boolean isServerAvailable() {
        try (Connection conn = connect()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}