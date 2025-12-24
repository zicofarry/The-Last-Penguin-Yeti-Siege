/**
 * Copyright (c) 2025 Muhammad 'Azmi Salam. All Rights Reserved.
 * Email: mhmmdzmslm36@gmail.com
 * GitHub: https://github.com/zicofarry
 */
package com.lastpenguin;

import com.lastpenguin.model.GameSettings;
import com.lastpenguin.model.Player;
import com.lastpenguin.model.SQLiteManager;
import com.lastpenguin.presenter.GamePresenter;
import com.lastpenguin.view.GamePanel;
import com.lastpenguin.view.GameWindow;
import com.lastpenguin.view.MenuPanel;
import com.lastpenguin.view.SettingsPanel;
import javax.swing.SwingUtilities;

/**
 * Orchestrator class for The Last Penguin: Yeti Siege.
 * Manages the transitions between menus and the gameplay state.
 * * @author Muhammad 'Azmi Salam
 * @version 1.0
 * @since December 2025
 */
public class Main {

    private static GameWindow window;
    private static GameSettings currentSettings;
    
    // Fix: Moved to static field to avoid lambda initialization error
    private static SettingsPanel settingsView; 

    public static void main(String[] args) {
        SQLiteManager.initDatabase();
        currentSettings = new GameSettings();

        SwingUtilities.invokeLater(() -> {
            window = new GameWindow();
            window.initWindow();
            showMenu();
        });
    }

    public static void showMenu() {
        MenuPanel menu = new MenuPanel(
            e -> startGame(),
            e -> showSettings()
        );
        window.setView(menu);
    }

    /**
     * Navigates the user to the Settings Screen.
     */
    public static void showSettings() {
        // Now using the static field 'settingsView'
        settingsView = new SettingsPanel(e -> {
            // Update settings model from view data
            currentSettings.setDifficulty(settingsView.getSelectedDifficulty());
            currentSettings.setMode(settingsView.getSelectedMode());
            currentSettings.setMusicVolume(settingsView.getVolume());
            
            // Return to menu after saving
            showMenu(); 
        });
        window.setView(settingsView);
    }

    public static void startGame() {
        Player player = new Player("Creativity");
        GamePanel gamePanel = new GamePanel();
        GamePresenter presenter = new GamePresenter(player, gamePanel, currentSettings);
        
        gamePanel.setPresenter(presenter);
        window.setView(gamePanel);
        presenter.startGame();
    }
}