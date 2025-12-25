/**
 * Copyright (c) 2025 Muhammad 'Azmi Salam. All Rights Reserved.
 * Email: mhmmdzmslm36@gmail.com
 * GitHub: https://github.com/zicofarry
 */
package com.lastpenguin.view;
import com.lastpenguin.model.SQLiteManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * The main menu screen.
 * Provides navigation to Start Game, Settings, and Exit.
 * * @author Muhammad 'Azmi Salam
 */
public class MenuPanel extends JPanel {
    private JTextField usernameField;
    private JTable leaderboardTable;
    private JScrollPane scrollPane;

    public MenuPanel(ActionListener playAction, ActionListener settingsAction) {
        setLayout(new BorderLayout(10, 10)); 
        setBackground(new Color(200, 230, 255));
        setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JLabel title = new JLabel("THE LAST PENGUIN: YETI SIEGE", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        add(title, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        
        // --- TAB FILTER DIFFICULTY ---
        JPanel tabPanel = new JPanel(new GridLayout(1, 3));
        JButton btnEasy = new JButton("EASY");
        JButton btnMed = new JButton("MEDIUM");
        JButton btnHard = new JButton("HARD");

        btnEasy.addActionListener(e -> refreshLeaderboard("EASY"));
        btnMed.addActionListener(e -> refreshLeaderboard("MEDIUM"));
        btnHard.addActionListener(e -> refreshLeaderboard("HARD"));

        tabPanel.add(btnEasy); tabPanel.add(btnMed); tabPanel.add(btnHard);
        centerPanel.add(tabPanel, BorderLayout.NORTH);

        // Leaderboard Table
        leaderboardTable = new JTable(SQLiteManager.getLeaderboardData("EASY"));
        scrollPane = new JScrollPane(leaderboardTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Agar bisa klik nama di tabel lalu masuk ke textfield
        leaderboardTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = leaderboardTable.getSelectedRow();
                if (row != -1) usernameField.setText(leaderboardTable.getValueAt(row, 0).toString());
            }
        });
        
        centerPanel.add(new JScrollPane(leaderboardTable), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setOpaque(false);
        inputPanel.add(new JLabel("USERNAME:"));
        usernameField = new JTextField(15);
        inputPanel.add(usernameField);
        centerPanel.add(inputPanel, BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);

        // BAWAH: BUTTONS
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        
        JButton btnPlay = new JButton("START GAME");
        JButton btnSettings = new JButton("SETTINGS");
        JButton btnExit = new JButton("EXIT");

        btnPlay.addActionListener(playAction);
        btnSettings.addActionListener(settingsAction);
        btnExit.addActionListener(e -> System.exit(0));

        buttonPanel.add(btnPlay);
        buttonPanel.add(btnSettings);
        buttonPanel.add(btnExit);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void refreshLeaderboard(String diff) {
        leaderboardTable.setModel(SQLiteManager.getLeaderboardData(diff));
    }
    public String getUsername() { return usernameField.getText().trim(); }
}