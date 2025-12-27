package com.lastpenguin;

import com.lastpenguin.model.*;
import com.lastpenguin.view.*;
import com.lastpenguin.presenter.GamePresenter;
import javax.swing.*;
import java.awt.event.ActionListener;

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
        GameSettings settings = SQLiteManager.loadSettings();
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
        menuView.refreshLeaderboard(settings.getDifficulty());
        window.setView(menuView);
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
        Player player = new Player(username); 
        final GamePanel[] gamePanelRef = new GamePanel[1];

        // 1. Aksi untuk Keluar ke Menu Utama
        ActionListener quitAction = e -> showMenu();

        // 2. Aksi untuk Buka Settings di Tengah Game
        ActionListener settingsAction = e -> showSettings(true, () -> {
            window.setView(gamePanelRef[0]);
            gamePanelRef[0].requestFocusInWindow();
        });

        // 3. Aksi untuk Restart Game (Play Again)
        ActionListener restartAction = e -> startGame(username);

        // Update: Inisialisasi GamePanel dengan 3 parameter Action
        gamePanelRef[0] = new GamePanel(quitAction, settingsAction, restartAction);
        
        // Callback saat game berakhir (Hanya simpan data, UI dihandle GamePanel)
        Runnable onGameOver = () -> {
            SQLiteManager.saveScore(
                player.getUsername(),
                player.getScore(),
                player.getYetiKilled(),
                player.getMissedShots(),
                player.getRemainingBullets(),
                currentSettings.getDifficulty(),
                currentSettings.getMode()
            );
            // Kita tidak memanggil showMenu() di sini agar panel Game Over es tetap terlihat
        };

        GamePresenter presenter = new GamePresenter(player, gamePanelRef[0], currentSettings, onGameOver);
        gamePanelRef[0].setPresenter(presenter);
        
        window.setView(gamePanelRef[0]);
        gamePanelRef[0].requestFocusInWindow(); 
        presenter.startGame();
    }
}