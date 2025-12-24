/**
 * Copyright (c) 2025 Muhammad 'Azmi Salam. All Rights Reserved.
 * Email: mhmmdzmslm36@gmail.com
 * GitHub: https://github.com/zicofarry
 */
package com.lastpenguin.view;

import javax.swing.JFrame;

/**
 * The main window frame for The Last Penguin: Yeti Siege.
 * Manages window properties and switches between different panels.
 * * @author Muhammad 'Azmi Salam
 * @version 1.0
 * @since December 2025
 */
public class GameWindow extends JFrame {

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    /**
     * Initializes the main game window.
     */
    public void initWindow() {
        setTitle("The Last Penguin: Yeti Siege");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setResizable(false);
        
        // Window is initially invisible until a panel is added
        setVisible(true);
    }
    
    /**
     * Replaces the current content pane with a new panel.
     * @param panel The new JPanel to display.
     */
    public void setView(javax.swing.JPanel panel) {
        getContentPane().removeAll();
        add(panel);
        revalidate();
        repaint();
        panel.requestFocusInWindow();
    }
}
