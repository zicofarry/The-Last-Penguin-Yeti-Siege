package com.lastpenguin;

import com.lastpenguin.model.*;
import com.lastpenguin.view.*;
import com.lastpenguin.presenter.GamePresenter;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.List;

public class Main {
    private static GameWindow window;
    private static GameSettings currentSettings;
    private static MenuPanel menuView; 
    private static SettingsPanel settingsView;
    private static Sound soundManager = new Sound();

    public static void main(String[] args) {
        SQLiteManager.initDatabase();
        currentSettings = SQLiteManager.loadSettings();
        soundManager.setSettings(currentSettings);

        SwingUtilities.invokeLater(() -> {
            window = new GameWindow();
            window.initWindow();
            showMenu();
        });
    }

    public static void showMenu() {
        soundManager.playMusic("bgm_main.wav");
        currentSettings = SQLiteManager.loadSettings(); // Pastikan settings terbaru dimuat
        
        menuView = new MenuPanel(
            e -> {
                if (menuView.getUsername().isEmpty() || menuView.getUsername().equals("Player Name")) {
                    JOptionPane.showMessageDialog(window, "Isi username dulu!");
                } else {
                    startGame(menuView.getUsername()); 
                }
            },
            e -> showSettings(false, () -> showMenu())
        );
        
        // Logika Leaderboard: Ambil dari MySQL jika Online, SQLite jika Offline
        refreshMenuLeaderboard();
        
        window.setView(menuView);
    }

    /**
     * Helper untuk mengisi data leaderboard di MenuPanel berdasarkan mode aktif
     */
    private static void refreshMenuLeaderboard() {
        List<Object[]> data;
        if (currentSettings.getMode().equals(GameSettings.ONLINE)) {
            data = MySQLManager.getGlobalLeaderboard(currentSettings.getDifficulty());
            System.out.println("[INFO] Memuat Leaderboard Global (MySQL)");
        } else {
            data = SQLiteManager.getLeaderboardData(currentSettings.getDifficulty());
            System.out.println("[INFO] Memuat Leaderboard Lokal (SQLite)");
        }
        menuView.setLeaderboardData(data);
    }

    public static void showSettings(boolean isIngame, Runnable onBackAction) {
        Runnable autosave = () -> {
            if (settingsView == null) return;

            boolean wasMusicOn = currentSettings.getMusicVolume() > 0;

            currentSettings.setDifficulty(settingsView.getSelectedDifficulty());
            currentSettings.setMode(settingsView.getSelectedMode());
            
            boolean isMusicOnNow = settingsView.isMusicEnabled();
            currentSettings.setMusicVolume(isMusicOnNow ? 100 : 0);
            
            currentSettings.setSfxVolume(settingsView.isSfxEnabled() ? 100 : 0);
            currentSettings.setUseMouse(settingsView.isMouseEnabled());
            currentSettings.setKeyS1(settingsView.getS1Key());
            currentSettings.setKeyS2(settingsView.getS2Key());
            currentSettings.setKeyS3(settingsView.getS3Key());

            SQLiteManager.updateSettings(currentSettings);

            if (isMusicOnNow != wasMusicOn) {
                if (isMusicOnNow) {
                    soundManager.playMusic("bgm_main.wav");
                } else {
                    soundManager.stopMusic();
                }
            }
        };

        settingsView = new SettingsPanel(currentSettings, isIngame, autosave, e -> {
            onBackAction.run(); 
        });
        window.setView(settingsView);
    }

    public static void startGame(String username) {
        String diff = currentSettings.getDifficulty();
        int initialBullets = SQLiteManager.getLastBulletCount(username, diff);
        
        Player player = new Player(username, initialBullets); 
        final GamePanel[] gamePanelRef = new GamePanel[1];

        ActionListener quitAction = e -> showMenu();
        ActionListener settingsAction = e -> showSettings(true, () -> {
            window.setView(gamePanelRef[0]);
            gamePanelRef[0].requestFocusInWindow();
        });
        ActionListener restartAction = e -> startGame(username);

        gamePanelRef[0] = new GamePanel(quitAction, settingsAction, restartAction);
        
        Runnable onGameOver = () -> {
            // 1. Simpan ke SQLite Lokal (Selalu dilakukan sebagai backup/riwayat)
            SQLiteManager.saveScore(
                player.getUsername(),
                player.getScore(),
                player.getYetiKilled(),
                player.getMissedShots(),
                player.getRemainingBullets(),
                currentSettings.getDifficulty(),
                currentSettings.getMode()
            );

            // 2. Simpan ke MySQL Online jika mode Online aktif
            if (currentSettings.getMode().equals(GameSettings.ONLINE)) {
                // Menjalankan proses jaringan di thread berbeda agar UI tidak freeze
                new Thread(() -> {
                    MySQLManager.saveScoreOnline(
                        player.getUsername(),
                        player.getScore(),
                        player.getYetiKilled(),
                        player.getMissedShots(),
                        player.getRemainingBullets(),
                        currentSettings.getDifficulty()
                    );
                }).start();
            }
        };

        GamePresenter presenter = new GamePresenter(player, gamePanelRef[0], currentSettings, onGameOver);
        gamePanelRef[0].setPresenter(presenter);
        
        window.setView(gamePanelRef[0]);
        gamePanelRef[0].requestFocusInWindow(); 
        presenter.startGame();
    }
}