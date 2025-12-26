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
import java.awt.event.ActionListener;

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
            e -> showSettings(false, () -> showMenu()) // Mode menu utama: bisa ubah difficulty
        );
        window.setView(menuView);
    }

    /**
     * Menampilkan panel pengaturan.
     * @param isIngame Jika true, maka difficulty dan mode tidak bisa diubah.
     * @param onBackAction Aksi yang dijalankan saat tombol SAVE diklik.
     */
    public static void showSettings(boolean isIngame, Runnable onBackAction) {
        settingsView = new SettingsPanel(currentSettings, isIngame, e -> {
            // Ambil nilai dari komponen UI SettingsPanel dan simpan ke currentSettings
            currentSettings.setDifficulty(settingsView.getSelectedDifficulty());
            currentSettings.setMode(settingsView.getSelectedMode());
            currentSettings.setMusicVolume(settingsView.isMusicEnabled() ? 100 : 0);
            currentSettings.setSfxVolume(settingsView.isSfxEnabled() ? 100 : 0);
            
            // Simpan pengaturan kontrol (Skill keys & Mouse toggle)
            currentSettings.setUseMouse(settingsView.isMouseEnabled());
            currentSettings.setKeyS1(settingsView.getS1Key());
            currentSettings.setKeyS2(settingsView.getS2Key());
            currentSettings.setKeyS3(settingsView.getS3Key());
            
            // Update ke database SQLite agar tersimpan permanen
            SQLiteManager.updateSettings(currentSettings);
            
            // Kembali ke layar sebelumnya (Menu atau Game)
            onBackAction.run();
        });
        window.setView(settingsView);
    }

    /**
     * Memulai sesi permainan baru.
     */
    public static void startGame(String username) {
        Player player = new Player(username); 
        
        // Kita butuh referensi final agar bisa diakses di dalam lambda settingsAction
        final GamePanel[] gamePanelRef = new GamePanel[1];

        // Aksi saat pemain memilih Quit dari menu pause
        ActionListener quitAction = e -> showMenu();

        // Aksi saat pemain memilih Settings dari menu pause
        ActionListener settingsAction = e -> showSettings(true, () -> {
            // Setelah save di settings, kembalikan tampilan ke panel game yang tadi
            window.setView(gamePanelRef[0]);
            gamePanelRef[0].requestFocusInWindow();
        });

        // Inisialisasi GamePanel dengan dua listener (Quit dan Settings)
        gamePanelRef[0] = new GamePanel(quitAction, settingsAction);
        
        // Callback saat game over (mati atau surrender)
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

        // Hubungkan model, view, dan presenter
        GamePresenter presenter = new GamePresenter(player, gamePanelRef[0], currentSettings, onGameOver);
        gamePanelRef[0].setPresenter(presenter);
        
        // Tampilkan game di window
        window.setView(gamePanelRef[0]);
        gamePanelRef[0].requestFocusInWindow(); 
        
        presenter.startGame();
    }
}