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

/**
 * Manages SQLite database operations including connection and schema initialization.
 * Simulates ENUM types using SQL CHECK constraints for data integrity.
 * * @author Muhammad 'Azmi Salam
 */
public class SQLiteManager {
    private static final String DB_URL = "jdbc:sqlite:data/antarctica.db";
    private static final String DATA_DIR = "data";

    /**
     * Establishes a connection to the SQLite database.
     * @return Connection object or null if failed.
     */
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

    /**
     * Initializes the database structure and default settings.
     * Creates the 'data' directory if it does not exist.
     */
    public static void initDatabase() {
        // Ensure data directory exists
        File directory = new File(DATA_DIR);
        if (!directory.exists()) {
            directory.mkdir();
        }

        // DDL: Create Players Table
        String createPlayersTable = "CREATE TABLE IF NOT EXISTS players (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL" +
                ");";

        // DDL: Create Scores Table with ENUM-like constraints
        String createScoresTable = "CREATE TABLE IF NOT EXISTS scores (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "player_id INTEGER NOT NULL," +
                "score INTEGER NOT NULL," +
                "missed_shots INTEGER NOT NULL," +
                "remaining_bullets INTEGER NOT NULL," +
                "yeti_killed INTEGER NOT NULL," +
                "difficulty TEXT NOT NULL CHECK(difficulty IN ('EASY', 'MEDIUM', 'HARD'))," +
                "mode TEXT NOT NULL CHECK(mode IN ('OFFLINE', 'ONLINE'))," +
                "played_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (player_id) REFERENCES players(id)" +
                ");";

        // DDL: Create Settings Table
        String createSettingsTable = "CREATE TABLE IF NOT EXISTS settings (" +
                "id INTEGER PRIMARY KEY CHECK (id = 1)," +
                "music_volume INTEGER DEFAULT 50," +
                "sfx_volume INTEGER DEFAULT 50," +
                "last_difficulty TEXT DEFAULT 'EASY' CHECK(last_difficulty IN ('EASY', 'MEDIUM', 'HARD'))," +
                "last_mode TEXT DEFAULT 'OFFLINE' CHECK(last_mode IN ('OFFLINE', 'ONLINE'))" +
                ");";

        // Default settings initialization
        String seedSettings = "INSERT OR IGNORE INTO settings (id, music_volume, sfx_volume, last_difficulty, last_mode) " +
                "VALUES (1, 50, 50, 'EASY', 'OFFLINE');";

        try (Connection conn = connect()) {
            if (conn != null) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createPlayersTable);
                    stmt.execute(createScoresTable);
                    stmt.execute(createSettingsTable);
                    stmt.execute(seedSettings);
                    System.out.println("Database Schema successfully initialized.");
                }
            } else {
                System.err.println("Critical Error: Connection is null. Database could not be initialized.");
            }
        } catch (SQLException e) {
            System.err.println("Schema Initialization Error: " + e.getMessage());
        }
    }
}
