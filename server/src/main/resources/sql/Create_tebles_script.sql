USE DB_Hitster;

-- ======================
-- USERS
-- ======================
CREATE TABLE
    Users (
        user_id INT AUTO_INCREMENT PRIMARY KEY,
        username VARCHAR(50) NOT NULL UNIQUE,
        email VARCHAR(100) NOT NULL UNIQUE,
        password_hash VARCHAR(255) NOT NULL,
        total_winnings DECIMAL(10, 2) DEFAULT 0,
        is_admin BOOLEAN DEFAULT FALSE
    );

-- ======================
-- SONGS
-- ======================
CREATE TABLE
    Songs (
        song_id INT AUTO_INCREMENT PRIMARY KEY,
        title VARCHAR(200) NOT NULL,
        artist VARCHAR(200) NOT NULL,
        release_year YEAR,
        song_path VARCHAR(500),
        cover_path VARCHAR(500)
    );

-- ======================
-- GAMES
-- ======================
CREATE TABLE
    Games (
        game_id INT AUTO_INCREMENT PRIMARY KEY,
        game_date DATETIME DEFAULT CURRENT_TIMESTAMP,
        player1_id INT NOT NULL,
        player2_id INT NOT NULL,
        player1_score INT DEFAULT 0,
        player2_score INT DEFAULT 0,
        current_turn INT,
        status VARCHAR(20) DEFAULT 'waiting',
        winner_id INT,
        -- FOREIGN KEY ref
        CONSTRAINT FK1_Games_Player1 FOREIGN KEY (player1_id) REFERENCES Users (user_id),
        CONSTRAINT FK2_Games_Player2 FOREIGN KEY (player2_id) REFERENCES Users (user_id),
        CONSTRAINT FK3_Games_Turn FOREIGN KEY (current_turn) REFERENCES Users (user_id),
        CONSTRAINT FK4_Games_Winner FOREIGN KEY (winner_id) REFERENCES Users (user_id)
    );

-- ======================
-- GAME_SONGS
-- ======================
CREATE TABLE
    Game_Songs (
        game_id INT NOT NULL,
        song_id INT NOT NULL,
        is_played BOOLEAN DEFAULT FALSE,
        on_board BOOLEAN DEFAULT FALSE,
        PRIMARY KEY (game_id, song_id),
        -- FOREIGN KEY ref
        CONSTRAINT FK1_GameSongs_Game FOREIGN KEY (game_id) REFERENCES Games (game_id) ON DELETE CASCADE,
        CONSTRAINT FK2_GameSongs_Song FOREIGN KEY (song_id) REFERENCES Songs (song_id) ON DELETE CASCADE
    );