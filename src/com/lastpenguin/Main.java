/**
 * Copyright (c) 2025 Muhammad 'Azmi Salam. All Rights Reserved.
 * Email: mhmmdzmslm36@gmail.com
 * GitHub: https://github.com/zicofarry
 */
package com.lastpenguin;

import com.lastpenguin.model.*;
import com.lastpenguin.view.*;
import com.lastpenguin.presenter.GamePresenter;
import javax.swing.*; // MENGGUNAKAN * AGAR SEMUA KOMPONEN SWING (SwingUtilities, JOptionPane, dll) TER-IMPORT

/**
 * Orchestrator class for The Last Penguin: Yeti Siege.
 * Manages the transitions between menus and the gameplay state.
 */
public class Main {
    private static GameWindow window;
    private static GameSettings currentSettings;
    private static MenuPanel menuView; 
    private static SettingsPanel settingsView;


    public static void main(String[] args) {
        SQLiteManager.initDatabase();
        currentSettings = SQLiteManager.loadSettings();

        SwingUtilities.invokeLater(() -> {
            window = new GameWindow();
            window.initWindow();
            showMenu();
        });
    }

    public static void showMenu() {
        // Refresh data leaderboard setiap kali kembali ke menu utama
        menuView = new MenuPanel(
            e -> {
                if (menuView.getUsername().isEmpty()) {
                    JOptionPane.showMessageDialog(window, "Isi username dulu!");
                } else {
                    startGame(menuView.getUsername()); 
                }
            },
            e -> showSettings()
        );
        window.setView(menuView);
    }

   // Di dalam Main.java
    public static void showSettings() {
        // Kirim currentSettings ke konstruktor SettingsPanel
        settingsView = new SettingsPanel(currentSettings, e -> {
            currentSettings.setDifficulty(settingsView.getSelectedDifficulty());
            currentSettings.setMode(settingsView.getSelectedMode());
            currentSettings.setMusicVolume(settingsView.getVolume());
            
            // Simpan ke database
            SQLiteManager.updateSettings(currentSettings);
            
            showMenu(); 
        });
        window.setView(settingsView);
    }

    public static void startGame(String username) {
        Player player = new Player(username); // Objek player baru untuk sesi ini
        GamePanel gamePanel = new GamePanel(e -> {}, e -> showMenu());
        
        // Callback yang akan dipanggil oleh Presenter saat game over
        Runnable onGameOver = () -> {
            System.out.println("DEBUG: Menyimpan skor untuk " + player.getUsername());
            SQLiteManager.saveScore(
                player.getUsername(),
                player.getScore(),
                player.getYetiKilled(),
                player.getMissedShots(),
                player.getRemainingBullets(),
                currentSettings.getDifficulty(),
                currentSettings.getMode()
            );
            showMenu(); // Kembali ke menu utama (Leaderboard akan refresh)
        };

        GamePresenter presenter = new GamePresenter(player, gamePanel, currentSettings, onGameOver);
        gamePanel.setPresenter(presenter);
        window.setView(gamePanel);
        presenter.startGame();
    }
}