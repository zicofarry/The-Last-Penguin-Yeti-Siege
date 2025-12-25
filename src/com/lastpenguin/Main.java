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

   public static void showSettings() {
        settingsView = new SettingsPanel(e -> {
            // Update objek settings
            currentSettings.setDifficulty(settingsView.getSelectedDifficulty());
            currentSettings.setMode(settingsView.getSelectedMode());
            currentSettings.setMusicVolume(settingsView.getVolume());
            
            // FIX: Simpan ke database agar tidak kembali ke EASY terus
            SQLiteManager.updateSettings(currentSettings);
            
            showMenu(); 
        });
        window.setView(settingsView);
    }

    public static void startGame(String username) {
        Player player = new Player(username);
        
        // Callback untuk kembali ke menu saat Game Over
        Runnable onGameOver = () -> {
            // SIMPAN SKOR TERAKHIR KE DATABASE
            // Gunakan nilai dari objek player dan settings saat ini
            SQLiteManager.saveScore(
                player.getUsername(), 
                player.getScore(), 
                player.getYetiKilled(), 
                currentSettings.getDifficulty(), 
                currentSettings.getMode()
            );
            showMenu(); // Kembali ke Leaderboard
        };

        // Callback untuk Menu Pause
        GamePanel gamePanel = new GamePanel(
            e -> {}, // Resume ditangani otomatis oleh InputHandler (menekan P lagi)
            e -> showMenu() // Tombol Quit kembali ke menu
        );

        // Kirim callback onGameOver ke presenter
        GamePresenter presenter = new GamePresenter(player, gamePanel, currentSettings, onGameOver);
        
        gamePanel.setPresenter(presenter);
        window.setView(gamePanel);
        presenter.startGame();
    }
}