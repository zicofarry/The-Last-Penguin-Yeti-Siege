/**
 * Copyright (c) 2025 Muhammad 'Azmi Salam. All Rights Reserved.
 * Email: mhmmdzmslm36@gmail.com
 * GitHub: https://github.com/zicofarry
 */
package com.lastpenguin.view;

import com.lastpenguin.model.GameSettings;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * UI for modifying game preferences including difficulty and online/offline mode.
 * Updated to reflect current settings from the database upon initialization.
 */
public class SettingsPanel extends JPanel {

    private JComboBox<String> diffBox;
    private JComboBox<String> modeBox;
    private JSlider volumeSlider;

    /**
     * Constructor updated to accept current settings for UI synchronization.
     * @param current The current GameSettings loaded from database.
     * @param backAction The action to perform when Save & Back is clicked.
     */
    public SettingsPanel(GameSettings current, ActionListener backAction) {
        setLayout(new GridLayout(5, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));
        setBackground(new Color(220, 240, 255));

        // Options arrays
        String[] difficulties = {GameSettings.EASY, GameSettings.MEDIUM, GameSettings.HARD};
        String[] modes = {GameSettings.OFFLINE, GameSettings.ONLINE};

        // Initialize components
        diffBox = new JComboBox<>(difficulties);
        modeBox = new JComboBox<>(modes);
        volumeSlider = new JSlider(0, 100, 50);
        
        // FIX: Set initial values based on current settings from database
        diffBox.setSelectedItem(current.getDifficulty());
        modeBox.setSelectedItem(current.getMode());
        volumeSlider.setValue(current.getMusicVolume());

        JButton btnSave = new JButton("SAVE & BACK");
        btnSave.addActionListener(backAction);

        // Styling for labels
        JLabel lblDiff = new JLabel("Game Difficulty:");
        JLabel lblMode = new JLabel("Game Mode:");
        JLabel lblVol = new JLabel("Music Volume:");
        
        // Add components to panel
        add(lblDiff);
        add(diffBox);
        add(lblMode);
        add(modeBox);
        add(lblVol);
        add(volumeSlider);
        add(new JLabel("")); // Spacer
        add(btnSave);
    }

    public String getSelectedDifficulty() { return (String) diffBox.getSelectedItem(); }
    public String getSelectedMode() { return (String) modeBox.getSelectedItem(); }
    public int getVolume() { return volumeSlider.getValue(); }
}