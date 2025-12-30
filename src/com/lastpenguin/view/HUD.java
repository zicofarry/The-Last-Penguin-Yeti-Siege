package com.lastpenguin.view;

import com.lastpenguin.model.Player;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Manages the rendering of the Heads-Up Display (HUD).
 * This class handles the visual representation of player statistics, 
 * including username, score, ammunition, and skill cooldown states 
 * with optimized aspect ratios and consistent typography.
 */
public class HUD {
    private Font mainFont;
    private Font labelFont;
    
    /** Consistent dark color scheme used for UI text elements to ensure readability. */
    private final Color DARK_TEXT = new Color(40, 45, 50); 

    public HUD() {
        this.mainFont = new Font("Arial", Font.BOLD, 18);
        this.labelFont = new Font("Arial", Font.PLAIN, 14);
    }

    /**
     * Primary rendering method for the HUD.
     * Orchestrates the drawing of the player status bar and skill interaction icons.
     */
    public void draw(Graphics g, Player player, Font icyFont, BufferedImage bar, BufferedImage s1, BufferedImage s2, BufferedImage s3, BufferedImage ballIcon) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Font displayFont = (icyFont != null) ? icyFont : mainFont;

        // --- 1. Player Status Bar Rendering (Top-Left) ---
        if (bar != null) {
            // Configures dimensions to maintain the original asset proportions
            int barWidth = 200;
            int barHeight = 150; 
            int startX = 5;
            int startY = 0;

            g2.drawImage(bar, startX, startY, barWidth, barHeight, null);
            
            g2.setColor(DARK_TEXT);
            
            // Render Player Username
            g2.setFont(displayFont.deriveFont(Font.BOLD, 23f));
            g2.drawString(player.getUsername(), startX + 65, startY + 45);
            
            // Render Current Game Score
            g2.setFont(displayFont.deriveFont(13f));
            g2.drawString("SCORE: " + player.getScore(), startX + 40, startY + 75);
            
            // Render Remaining Ammunition
            g2.drawString("BULLETS: " + player.getRemainingBullets(), startX + 40, startY + 105);
        }

        // --- 2. Skill Icons Management (Top-Right) ---
        int skillStartX = 480; 
        int skillY = 20;
        int spacing = 100; 

        // Renders individual skill items with specific ammunition requirements
        drawSkillItem(g2, s1, ballIcon, "GIANT 5", player.getCooldownS1(), 5, player.getRemainingBullets(), skillStartX, skillY, displayFont);
        drawSkillItem(g2, s2, ballIcon, "METEOR 10", player.getCooldownS2(), 10, player.getRemainingBullets(), skillStartX + spacing, skillY, displayFont);
        drawSkillItem(g2, s3, ballIcon, "INVISIBLE 3", player.getCooldownS3(), 3, player.getRemainingBullets(), skillStartX + (spacing * 2), skillY, displayFont);
    }

    /**
     * Renders a single skill unit, including the icon, cooldown timer, 
     * and requirement labels.
     */
    private void drawSkillItem(Graphics2D g2, BufferedImage icon, BufferedImage ball, String label, int cd, int req, int ammo, int x, int y, Font font) {
        boolean canUse = (cd == 0 && ammo >= req);
        int size = 80; 

        // Updates visual transparency based on availability (Cooldown or Ammo constraints)
        if (!canUse) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        } else {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        // Draw the main skill icon
        if (icon != null) g2.drawImage(icon, x, y, size, size, null);

        // Render the numeric cooldown countdown if the skill is currently active/recharging
        if (cd > 0) {
            g2.setColor(DARK_TEXT);
            g2.setFont(font.deriveFont(Font.BOLD, 24f));
            FontMetrics fm = g2.getFontMetrics();
            String cdText = String.valueOf((cd / 60) + 1);
            int tx = x + (size - fm.stringWidth(cdText)) / 2;
            int ty = y + (size / 2) + (fm.getAscent() / 2) - 2;
            g2.drawString(cdText, tx, ty);
        }

        // Render skill description labels and ammunition icons below the skill graphic
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2.setFont(font.deriveFont(Font.BOLD, 11f));
        g2.setColor(DARK_TEXT); 
        
        String[] parts = label.split(" "); 
        String namePart = parts[0];
        String numPart = parts[1];
        
        int labelY = y + size + 18;
        int totalTextWidth = g2.getFontMetrics().stringWidth(namePart + " " + numPart) + 16;
        int startTextX = x + (size - totalTextWidth) / 2;

        g2.drawString(namePart + " " + numPart, startTextX, labelY);
        
        if (ball != null) {
            int ballX = startTextX + g2.getFontMetrics().stringWidth(namePart + " " + numPart) + 4;
            g2.drawImage(ball, ballX, labelY - 10, 12, 12, null);
        }
    }
}