package com.lastpenguin.view;

import javax.swing.JFrame;

/**
 * The primary window frame for the application.
 * Manages core window properties and handles switching between 
 * different graphical panels (Menu, Settings, and Game).
 */
public class GameWindow extends JFrame {

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    /**
     * Configures and displays the main game window.
     */
    public void initWindow() {
        setTitle("The Last Penguin: Yeti Siege");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setResizable(false);
        
        setVisible(true);
    }
    
    /**
     * Replaces the current visible content with a new graphical panel.
     * @param panel The JPanel to be displayed.
     */
    public void setView(javax.swing.JPanel panel) {
        getContentPane().removeAll();
        add(panel);
        revalidate();
        repaint();
        panel.requestFocusInWindow();
    }
}