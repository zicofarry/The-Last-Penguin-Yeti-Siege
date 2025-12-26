/**
 * Copyright (c) 2025 Muhammad 'Azmi Salam. All Rights Reserved.
 * Email: mhmmdzmslm36@gmail.com
 * GitHub: https://github.com/zicofarry
 */
package com.lastpenguin;

import com.lastpenguin.model.*;
import com.lastpenguin.view.*;
import com.lastpenguin.presenter.GamePresenter;
import javax.swing.*; 

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
        // Inisialisasi Database dan muat pengaturan yang tersimpan
        SQLiteManager.initDatabase();
        currentSettings = SQLiteManager.loadSettings();

        SwingUtilities.invokeLater(() -> {
            window = new GameWindow();
            window.initWindow();
            showMenu();
        });
    }

    /**
     * Menampilkan menu utama dengan leaderboard yang diperbarui.
     */
    public static void showMenu() {
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

    /**
     * Menampilkan panel pengaturan dan menyimpan perubahan (termasuk tombol kustom dan mouse).
     */
    public static void showSettings() {
        // Kirim currentSettings ke SettingsPanel agar UI sinkron
        settingsView = new SettingsPanel(currentSettings, e -> {
            // Ambil nilai dari komponen UI SettingsPanel
            currentSettings.setDifficulty(settingsView.getSelectedDifficulty());
            currentSettings.setMode(settingsView.getSelectedMode());
            currentSettings.setMusicVolume(settingsView.getVolume());
            
            // Simpan pengaturan kontrol baru (Skill keys & Mouse toggle)
            currentSettings.setUseMouse(settingsView.isMouseEnabled());
            currentSettings.setKeyS1(settingsView.getS1Key());
            currentSettings.setKeyS2(settingsView.getS2Key());
            currentSettings.setKeyS3(settingsView.getS3Key());
            
            // Simpan perubahan ke database SQLite
            SQLiteManager.updateSettings(currentSettings);
            
            showMenu(); 
        });
        window.setView(settingsView);
    }

    /**
     * Memulai sesi permainan baru.
     */
    public static void startGame(String username) {
        Player player = new Player(username); 
        // Menggunakan action listener tunggal untuk kembali ke menu
        GamePanel gamePanel = new GamePanel(e -> showMenu());
        
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
            showMenu();
        };

        GamePresenter presenter = new GamePresenter(player, gamePanel, currentSettings, onGameOver);
        gamePanel.setPresenter(presenter);
        
        window.setView(gamePanel);
        
        // PENTING: Minta fokus agar keyboard terbaca
        gamePanel.requestFocusInWindow(); 
        
        presenter.startGame();
    }
}