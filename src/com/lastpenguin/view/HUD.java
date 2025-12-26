/**
 * Copyright (c) 2025 Muhammad 'Azmi Salam. All Rights Reserved.
 * Email: mhmmdzmslm36@gmail.com
 * GitHub: https://github.com/zicofarry
 */
package com.lastpenguin.view;

import com.lastpenguin.model.Player;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/**
 * Responsible for rendering the Heads-Up Display (HUD) on the game screen.
 * Displays real-time statistics such as score, ammunition, and player info.
 * * @author Muhammad 'Azmi Salam
 * @version 1.0
 * @since December 2025
 */
public class HUD {

    private Font mainFont;
    private Font labelFont;

    /**
     * Initializes HUD fonts and styling.
     */
    public HUD() {
        this.mainFont = new Font("Arial", Font.BOLD, 18);
        this.labelFont = new Font("Arial", Font.PLAIN, 14);
    }

    /**
     * Draws the HUD elements onto the screen.
     * @param g The graphics object from GamePanel.
     * @param player The player instance to pull data from.
     */
    public void draw(Graphics g, Player player) {
        // Draw Semi-transparent background for readability
        g.setColor(new Color(0, 0, 0, 100)); // Black with 100/255 alpha
        g.fillRect(10, 10, 220, 120);

        // Draw Border
        g.setColor(Color.WHITE);
        g.drawRect(10, 10, 220, 120);

        // Set Text Color
        g.setColor(Color.WHITE);

        // 1. Username
        g.setFont(mainFont);
        g.drawString(player.getUsername(), 20, 35);

        // 2. Score
        g.setFont(labelFont);
        g.drawString("Score: " + player.getScore(), 20, 60);

        // 3. Ammunition (Remaining Bullets)
        g.drawString("Remaining Bullets: " + player.getRemainingBullets(), 20, 80);

        // 4. Missed Shots
        g.setColor(new Color(255, 150, 150)); // Light red for misses
        g.drawString("Missed Shots: " + player.getMissedShots(), 20, 100);

        // 5. Yeti Killed
        g.setColor(Color.CYAN);
        g.drawString("Yeti Killed: " + player.getYetiKilled(), 20, 120);
        
        // Draw Health Bar (Bonus for visual feedback)
        drawHealthBar(g, player.getHealth());
        int startX = 600, startY = 500;
        g.setFont(labelFont);
        drawSkillIcon(g, "1. Snowball", player.getCooldownS1(), startX, startY);
        drawSkillIcon(g, "2. Meteor", player.getCooldownS2(), startX, startY + 25);
        drawSkillIcon(g, "3. Ghost", player.getCooldownS3(), startX, startY + 50);
    }

    /**
     * Visual representation of player's health.
     */
    private void drawHealthBar(Graphics g, int health) {
        g.setColor(Color.GRAY);
        g.fillRect(580, 20, 200, 20); // Background
        
        if (health > 50) g.setColor(Color.GREEN);
        else if (health > 25) g.setColor(Color.YELLOW);
        else g.setColor(Color.RED);
        
        g.fillRect(580, 20, (int) (health * 2), 20); // Health fill
        g.setColor(Color.WHITE);
        g.drawRect(580, 20, 200, 20); // Outline
        g.drawString("HP", 555, 35);
    }

    private void drawSkillIcon(Graphics g, String name, int cooldown, int x, int y) {
        g.setColor(cooldown > 0 ? Color.GRAY : Color.GREEN);
        String status = cooldown > 0 ? (cooldown/60) + "s" : "READY";
        g.drawString(name + ": " + status, x, y);
    }
}
