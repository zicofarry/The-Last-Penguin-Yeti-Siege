package com.lastpenguin.view;

import com.lastpenguin.model.Player;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * HUD dengan perbaikan aspect ratio player_bar dan teks warna gelap.
 */
public class HUD {
    private Font mainFont;
    private Font labelFont;
    // Warna gelap yang konsisten untuk seluruh UI
    private final Color DARK_TEXT = new Color(40, 45, 50); 

    public HUD() {
        this.mainFont = new Font("Arial", Font.BOLD, 18);
        this.labelFont = new Font("Arial", Font.PLAIN, 14);
    }

    public void draw(Graphics g, Player player, Font icyFont, BufferedImage bar, BufferedImage s1, BufferedImage s2, BufferedImage s3, BufferedImage ballIcon) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Font displayFont = (icyFont != null) ? icyFont : mainFont;

        // 1. Player Bar (Kiri Atas)
        if (bar != null) {
            // Ukuran disesuaikan agar tidak gepeng (Proporsi asli sekitar 350x190 atau skala yang mirip)
            int barWidth = 200;
            int barHeight = 150; 
            int startX = 5;
            int startY = 0;

            g2.drawImage(bar, startX, startY, barWidth, barHeight, null);
            
            // Set warna teks jadi Gelap
            g2.setColor(DARK_TEXT);
            
            // Username (Baris Atas)
            g2.setFont(displayFont.deriveFont(Font.BOLD, 23f));
            g2.drawString(player.getUsername(), startX + 65, startY + 45);
            
            // Score (Baris Tengah)
            g2.setFont(displayFont.deriveFont(13f));
            g2.drawString("SCORE: " + player.getScore(), startX + 40, startY + 75);
            
            // Bullets (Baris Bawah)
            g2.drawString("BULLETS: " + player.getRemainingBullets(), startX + 40, startY + 105);
        }

        // 2. Skill Icons (Kanan Atas)
        int skillStartX = 480; 
        int skillY = 20;
        int spacing = 100; 

        drawSkillItem(g2, s1, ballIcon, "GIANT 5", player.getCooldownS1(), 5, player.getRemainingBullets(), skillStartX, skillY, displayFont);
        drawSkillItem(g2, s2, ballIcon, "METEOR 10", player.getCooldownS2(), 10, player.getRemainingBullets(), skillStartX + spacing, skillY, displayFont);
        drawSkillItem(g2, s3, ballIcon, "INVISIBLE 3", player.getCooldownS3(), 3, player.getRemainingBullets(), skillStartX + (spacing * 2), skillY, displayFont);
    }

    private void drawSkillItem(Graphics2D g2, BufferedImage icon, BufferedImage ball, String label, int cd, int req, int ammo, int x, int y, Font font) {
        boolean canUse = (cd == 0 && ammo >= req);
        int size = 80; // Ukuran icon skill

        // Set Transparansi Icon
        if (!canUse) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        } else {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        // Gambar Icon Skill
        if (icon != null) g2.drawImage(icon, x, y, size, size, null);

        // A. Gambar Angka Cooldown
        if (cd > 0) {
            g2.setColor(DARK_TEXT);
            g2.setFont(font.deriveFont(Font.BOLD, 24f));
            FontMetrics fm = g2.getFontMetrics();
            String cdText = String.valueOf((cd / 60) + 1);
            int tx = x + (size - fm.stringWidth(cdText)) / 2;
            int ty = y + (size / 2) + (fm.getAscent() / 2) - 2;
            g2.drawString(cdText, tx, ty);
        }

        // B. Gambar Label Teks di Bawah Skill
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