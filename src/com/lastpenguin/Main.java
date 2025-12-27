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
    private static Sound soundManager = new Sound(); // Pastikan Sound Manager tersedia

    public static void main(String[] args) {
        SQLiteManager.initDatabase();
        currentSettings = SQLiteManager.loadSettings();
        soundManager.setSettings(currentSettings); // Hubungkan settings ke sound

        SwingUtilities.invokeLater(() -> {
            window = new GameWindow();
            window.initWindow();
            showMenu();
        });
    }

    public static void showMenu() {
        soundManager.playMusic("bgm_main.wav"); // Putar musik menu
        GameSettings settings = SQLiteManager.loadSettings();
        menuView = new MenuPanel(
            e -> {
                if (menuView.getUsername().isEmpty()) {
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
        // Logika Autosave
        Runnable autosave = () -> {
            if (settingsView == null) return;

            // 1. Simpan status musik sebelum diupdate
            boolean wasMusicOn = currentSettings.getMusicVolume() > 0;

            // 2. Sync UI ke model (Update semua pengaturan)
            currentSettings.setDifficulty(settingsView.getSelectedDifficulty());
            currentSettings.setMode(settingsView.getSelectedMode());
            
            boolean isMusicOnNow = settingsView.isMusicEnabled();
            currentSettings.setMusicVolume(isMusicOnNow ? 100 : 0);
            
            currentSettings.setSfxVolume(settingsView.isSfxEnabled() ? 100 : 0);
            currentSettings.setUseMouse(settingsView.isMouseEnabled());
            currentSettings.setKeyS1(settingsView.getS1Key());
            currentSettings.setKeyS2(settingsView.getS2Key());
            currentSettings.setKeyS3(settingsView.getS3Key());

            // 3. Simpan ke Database
            SQLiteManager.updateSettings(currentSettings);

            // 4. LOGIKA PERBAIKAN: Hanya ubah status musik jika ada perubahan ON/OFF
            if (isMusicOnNow != wasMusicOn) {
                if (isMusicOnNow) {
                    soundManager.playMusic("bgm_main.wav");
                } else {
                    soundManager.stopMusic();
                }
            }
        };

        settingsView = new SettingsPanel(currentSettings, isIngame, autosave, e -> {
            onBackAction.run(); // Tombol BACK
        });
        window.setView(settingsView);
    }

    public static void startGame(String username) {
        Player player = new Player(username); 
        final GamePanel[] gamePanelRef = new GamePanel[1];

        ActionListener quitAction = e -> showMenu();
        ActionListener settingsAction = e -> showSettings(true, () -> {
            window.setView(gamePanelRef[0]);
            gamePanelRef[0].requestFocusInWindow();
        });

        gamePanelRef[0] = new GamePanel(quitAction, settingsAction);
        
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
            showMenu();
        };

        GamePresenter presenter = new GamePresenter(player, gamePanelRef[0], currentSettings, onGameOver);
        gamePanelRef[0].setPresenter(presenter);
        
        window.setView(gamePanelRef[0]);
        gamePanelRef[0].requestFocusInWindow(); 
        presenter.startGame();
    }
}