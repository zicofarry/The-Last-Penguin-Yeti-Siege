-- 1. Membuat Database
CREATE DATABASE IF NOT EXISTS antarctica;
USE antarctica;

-- 2. Tabel Players (Sistem Identitas Tanpa Password)
CREATE TABLE IF NOT EXISTS players (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Tabel Scores (Data Sesi Permainan Online)
CREATE TABLE IF NOT EXISTS scores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_id INT NOT NULL,
    score INT NOT NULL,
    missed_shots INT NOT NULL,
    remaining_bullets INT NOT NULL,
    yeti_killed INT NOT NULL,
    difficulty ENUM('EASY', 'MEDIUM', 'HARD') NOT NULL,
    mode ENUM('OFFLINE', 'ONLINE') NOT NULL,
    played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- Foreign Key agar data integritas terjaga
    CONSTRAINT fk_score_player FOREIGN KEY (player_id) 
        REFERENCES players(id) ON DELETE CASCADE
);
