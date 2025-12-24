/**
 * Copyright (c) 2025 Muhammad 'Azmi Salam. All Rights Reserved.
 * Email: mhmmdzmslm36@gmail.com
 * GitHub: https://github.com/zicofarry
 */
package com.lastpenguin.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * The main menu screen.
 * Provides navigation to Start Game, Settings, and Exit.
 * * @author Muhammad 'Azmi Salam
 */
public class MenuPanel extends JPanel {

    public MenuPanel(ActionListener playAction, ActionListener settingsAction) {
        setLayout(new GridBagLayout());
        setBackground(new Color(200, 230, 255));

        JLabel title = new JLabel("THE LAST PENGUIN: YETI SIEGE");
        title.setFont(new Font("Arial", Font.BOLD, 36));
        
        JButton btnPlay = new JButton("START GAME");
        JButton btnSettings = new JButton("SETTINGS");
        JButton btnExit = new JButton("EXIT");

        // Action Listeners
        btnPlay.addActionListener(playAction);
        btnSettings.addActionListener(settingsAction);
        btnExit.addActionListener(e -> System.exit(0));

        // Layouting
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 0, 10, 0);

        gbc.gridy = 0; add(title, gbc);
        gbc.gridy = 1; add(btnPlay, gbc);
        gbc.gridy = 2; add(btnSettings, gbc);
        gbc.gridy = 3; add(btnExit, gbc);
    }
}
