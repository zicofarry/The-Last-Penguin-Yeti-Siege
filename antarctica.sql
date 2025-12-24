-- 1. Tabel Master Player (Menghindari Redundansi Nama)
CREATE TABLE IF NOT EXISTS players (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL
);

-- 2. Tabel Skor (Mencatat sesi permainan dengan detail)
CREATE TABLE IF NOT EXISTS scores (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_id INTEGER NOT NULL,
    score INTEGER NOT NULL,
    missed_shots INTEGER NOT NULL,
    remaining_bullets INTEGER NOT NULL,
    yeti_killed INTEGER NOT NULL,
    difficulty TEXT NOT NULL CHECK(difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    mode TEXT NOT NULL CHECK(mode IN ('OFFLINE', 'ONLINE')),
    played_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player_id) REFERENCES players(id)
);

-- 3. Tabel Pengaturan (Global State)
CREATE TABLE IF NOT EXISTS settings (
    id INTEGER PRIMARY KEY CHECK (id = 1), -- Memastikan hanya ada 1 baris setting
    music_volume INTEGER DEFAULT 50,
    sfx_volume INTEGER DEFAULT 50,
    last_difficulty TEXT DEFAULT 'EASY' CHECK(last_difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    last_mode TEXT DEFAULT 'OFFLINE' CHECK(last_mode IN ('OFFLINE', 'ONLINE'))
);
