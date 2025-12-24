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
 * * @author Muhammad 'Azmi Salam
 */
public class SettingsPanel extends JPanel {

    private JComboBox<String> diffBox;
    private JComboBox<String> modeBox;
    private JSlider volumeSlider;

    public SettingsPanel(ActionListener backAction) {
        setLayout(new GridLayout(5, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));

        // Components
        String[] difficulties = {GameSettings.EASY, GameSettings.MEDIUM, GameSettings.HARD};
        String[] modes = {GameSettings.OFFLINE, GameSettings.ONLINE};

        diffBox = new JComboBox<>(difficulties);
        modeBox = new JComboBox<>(modes);
        volumeSlider = new JSlider(0, 100, 50);

        JButton btnSave = new JButton("SAVE & BACK");
        btnSave.addActionListener(backAction);

        // Add to panel
        add(new JLabel("Game Difficulty:"));
        add(diffBox);
        add(new JLabel("Game Mode:"));
        add(modeBox);
        add(new JLabel("Music Volume:"));
        add(volumeSlider);
        add(new JLabel("")); 
        add(btnSave);
    }

    public String getSelectedDifficulty() { return (String) diffBox.getSelectedItem(); }
    public String getSelectedMode() { return (String) modeBox.getSelectedItem(); }
    public int getVolume() { return volumeSlider.getValue(); }
}
