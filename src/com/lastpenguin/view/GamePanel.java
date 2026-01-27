package com.lastpenguin.view;

import com.lastpenguin.model.*;
import com.lastpenguin.presenter.GamePresenter;
import com.lastpenguin.presenter.InputHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * The primary graphical interface for the game session.
 * This class manages the rendering of all game entities, environmental
 * obstacles,
 * and UI overlays for pause and game-over states.
 */
public class GamePanel extends JPanel {
    private GamePresenter presenter;
    private HUD hud = new HUD();
    private BufferedImage arenaImg, penguinSheet, lubangImg, rockImg, spikeImg;
    private BufferedImage ballPImg, ballYImg, ballGiantImg;
    private BufferedImage meteorImg, targetImg;
    private BufferedImage playerBarImg, skill1Img, skill2Img, skill3Img, aimArrowImg;
    private BufferedImage buffSkill1Vfx;
    private Image pauseBgImage;
    private Image gameOverBgImage;
    private Font customFont;
    private int spriteNum = 0, spriteCounter = 0;
    private JPanel pauseMenu;
    private JPanel gameOverMenu;
    private JLabel lblPauseStats;
    private JLabel lblGameOverStats;
    private BufferedImage[] yetiSprites;
    private Sound soundManager = new Sound();

    public GamePanel(ActionListener quitAction, ActionListener settingsAction, ActionListener restartAction) {
        setPreferredSize(new Dimension(800, 600));
        setDoubleBuffered(true);
        setFocusable(true);
        setLayout(null);
        loadAssets();

        // Initialize UI components for navigation and game state feedback
        setupUI(quitAction, settingsAction, restartAction);
    }

    /**
     * Initializes and configures the overlay menus for Pausing and Game Over
     * states.
     */
    private void setupUI(ActionListener quitAction, ActionListener settingsAction, ActionListener restartAction) {
        int pauseW = 520;
        int pauseH = 650;
        int pauseX = (800 - pauseW) / 2;
        int pauseY = (600 - pauseH) / 2;

        pauseMenu = createOverlayPanel(pauseX, pauseY, pauseW, pauseH, pauseBgImage);
        lblPauseStats = createStatsLabel(pauseW, 32f);
        lblPauseStats.setForeground(new Color(60, 40, 20));
        pauseMenu.add(lblPauseStats);

        int pBtnY = 492;
        int pBtnW = 150;
        pauseMenu.add(createStyledPauseButton("RESUME", 88, pBtnY, pBtnW, 40, e -> {
            soundManager.playEffect("sfx_click.wav");
            if (presenter != null) {
                presenter.getInput().setPaused(false);
                this.requestFocusInWindow();
            }
        }));
        pauseMenu.add(createStyledPauseButton("SETTINGS", 185, pBtnY, pBtnW, 40, e -> {
            soundManager.playEffect("sfx_click.wav");
            settingsAction.actionPerformed(e);
        }));
        pauseMenu.add(createStyledPauseButton("SURRENDER", 280, pBtnY, pBtnW, 40, e -> {
            soundManager.playEffect("sfx_click.wav");
            if (presenter != null) {
                presenter.surrender();
                presenter.getInput().setPaused(false);
                updatePauseUI(false);
            }
        }));
        add(pauseMenu);

        gameOverMenu = createOverlayPanel(0, 0, 800, 600, gameOverBgImage);
        lblGameOverStats = createStatsLabel(800, 40f);
        lblGameOverStats.setForeground(new Color(200, 240, 255));
        gameOverMenu.add(lblGameOverStats);

        int goBtnY = 380;
        int goBtnW = 260;
        gameOverMenu.add(createStyledGameOverButton("", 130, goBtnY, goBtnW, 70, e -> {
            soundManager.playEffect("sfx_click.wav");
            restartAction.actionPerformed(e);
        }));
        gameOverMenu.add(createStyledGameOverButton("", 430, goBtnY, goBtnW, 70, e -> {
            soundManager.playEffect("sfx_click.wav");
            quitAction.actionPerformed(e);
        }));
        add(gameOverMenu);
    }

    /**
     * Utility method to create a transparent panel with a background image.
     */
    private JPanel createOverlayPanel(int x, int y, int w, int h, Image bgImage) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                if (bgImage != null)
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
                super.paintComponent(g);
            }
        };
        panel.setBounds(x, y, w, h);
        panel.setOpaque(false);
        panel.setLayout(null);
        panel.setVisible(false);
        return panel;
    }

    private JLabel createStatsLabel(int panelWidth, float fontSize) {
        JLabel label = new JLabel("", SwingConstants.CENTER);
        label.setFont(customFont.deriveFont(fontSize));
        label.setBounds(0, 200, panelWidth, 220);
        return label;
    }

    private JButton createStyledPauseButton(String text, int x, int y, int w, int h, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(customFont.deriveFont(Font.BOLD, 14f));
        btn.setForeground(new Color(60, 40, 20));
        btn.setBounds(x, y, w, h);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        return btn;
    }

    private JButton createStyledGameOverButton(String text, int x, int y, int w, int h, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setBounds(x, y, w, h);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        return btn;
    }

    /**
     * Toggles the visibility of the Pause or Game Over menus based on game state.
     */
    public void updatePauseUI(boolean isPaused) {
        if (presenter != null && !presenter.getPlayer().isAlive()) {
            pauseMenu.setVisible(false);
            if (!gameOverMenu.isVisible()) {
                Player p = presenter.getPlayer();
                lblGameOverStats.setText(
                        "<html><div style='text-align: center;'>FINAL SCORE<br>" + p.getScore() + "</div></html>");
                gameOverMenu.setVisible(true);
            }
            return;
        }
        if (isPaused) {
            if (!pauseMenu.isVisible()) {
                Player p = presenter.getPlayer();
                lblPauseStats.setText("<html><div style='text-align: center;'>SCORE: " + p.getScore() +
                        "<br>BULLETS: " + p.getRemainingBullets() +
                        "<br>MISSES: " + p.getMissedShots() +
                        "<br>YETIS KILLED: " + p.getYetiKilled() + "</div></html>");
                pauseMenu.setVisible(true);
            }
        } else {
            pauseMenu.setVisible(false);
        }
    }

    /**
     * Loads required image assets and custom fonts from the resources directory.
     */
    private void loadAssets() {
        arenaImg = AssetLoader.loadImage("environment/arena.png");
        lubangImg = AssetLoader.loadImage("environment/lubang.png");
        rockImg = AssetLoader.loadImage("environment/obs_ice_rock.png");
        spikeImg = AssetLoader.loadImage("environment/obs_ice_spike.png");
        penguinSheet = AssetLoader.loadImage("sprites/penguin.png");
        yetiSprites = AssetLoader.loadYetiSprites();
        pauseBgImage = AssetLoader.loadImage("ui/pause_bg.png");
        gameOverBgImage = AssetLoader.loadImage("ui/game_over.png");
        ballPImg = AssetLoader.loadImage("projectiles/snowball_p.png");
        ballYImg = AssetLoader.loadImage("projectiles/snowball_y.png");
        ballGiantImg = AssetLoader.loadImage("projectiles/snowball_giant.png");
        meteorImg = AssetLoader.loadImage("projectiles/meteor.png");
        targetImg = AssetLoader.loadImage("vfx/meteor_target.png");
        playerBarImg = AssetLoader.loadImage("ui/player_bar.png");
        skill1Img = AssetLoader.loadImage("ui/skill1.png");
        skill2Img = AssetLoader.loadImage("ui/skill2.png");
        skill3Img = AssetLoader.loadImage("ui/skill3.png");
        aimArrowImg = AssetLoader.loadImage("vfx/aim_arrow.png");
        buffSkill1Vfx = AssetLoader.loadImage("vfx/buff_skill1.png");
        try {
            customFont = Font
                    .createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/assets/fonts/icy_font.ttf"))
                    .deriveFont(18f);
        } catch (Exception e) {
            customFont = new Font("Arial", Font.BOLD, 18);
        }
    }

    public void setPresenter(GamePresenter p) {
        this.presenter = p;
    }

    /**
     * Core drawing method called during the game loop.
     * Manages the rendering order to ensure entities appear correctly relative to
     * the environment.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (presenter == null)
            return;
        Graphics2D g2 = (Graphics2D) g;

        if (arenaImg != null)
            g.drawImage(arenaImg, 0, 0, 800, 600, null);

        // Rendering environmental obstacles
        for (Obstacle o : presenter.getObstacles()) {
            if (o.isHole()) {
                g.drawImage(lubangImg, o.getX(), o.getY(), o.getWidth(), o.getHeight(), null);
            } else {
                BufferedImage currentImg = (o.getHp() > 15) ? rockImg : spikeImg;
                int padding = 15;
                g.drawImage(currentImg, o.getX() - padding, o.getY() - padding, o.getWidth() + (padding * 2),
                        o.getHeight() + (padding * 2), null);

                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 14));
                String hpText = String.valueOf(o.getHp());
                FontMetrics fm = g.getFontMetrics();
                int textX = o.getX() + (o.getWidth() - fm.stringWidth(hpText)) / 2;
                int textY = o.getY() + (o.getHeight() + fm.getAscent()) / 2;

                g.setColor(new Color(0, 0, 0, 150));
                g.drawString(hpText, textX + 1, textY + 1);
                g.setColor(Color.WHITE);
                g.drawString(hpText, textX, textY);
            }
        }

        // Rendering special ability targeting and effects
        if (presenter.isTargetingMeteor()) {
            g.drawImage(targetImg, presenter.getInput().getMouseX() - 40, presenter.getInput().getMouseY() - 40, 80, 80,
                    null);
        }
        for (Meteor m : presenter.getActiveMeteors()) {
            g.drawImage(meteorImg, m.getX(), m.getY(), 80, 80, null);
        }

        // Rendering all active projectiles
        for (Projectile p : presenter.getProjectiles()) {
            BufferedImage cb = p.isPiercing() ? ballGiantImg
                    : (p.getOwner().equals(Projectile.YETI_TYPE) ? ballYImg : ballPImg);
            int sz = p.isPiercing() ? 80 : (p.getOwner().equals(Projectile.YETI_TYPE) ? 20 : 15);
            g.drawImage(cb, p.getX(), p.getY(), sz, sz, null);
        }

        // Rendering enemy units
        for (Yeti y : presenter.getYetis()) {
            int idx = y.getSpriteIndex();
            if (yetiSprites != null && idx < yetiSprites.length) {
                g.drawImage(yetiSprites[idx], y.getX(), y.getY(), 75, 75, null);
            }
        }

        // Rendering player character and associated visual effects
        if (presenter.getPlayer().isAlive()) {
            Player p = presenter.getPlayer();

            if (p.isGhost()) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            }

            // Skill 1 aura effect
            if (p.isGiantBuffActive() && buffSkill1Vfx != null) {
                int buffSize = 110;
                int bx = p.getX() + 25 - (buffSize / 2);
                int by = p.getY() + 25 - (buffSize / 2);

                float pulse = (float) Math.sin(System.currentTimeMillis() * 0.005) * 5;
                g2.drawImage(buffSkill1Vfx, (int) (bx - pulse / 2), (int) (by - pulse / 2), (int) (buffSize + pulse),
                        (int) (buffSize + pulse), null);
            }

            // Target indicator arrow
            if (aimArrowImg != null) {
                int px = p.getX() + 25;
                int py = p.getY() + 25;
                double angle = Math.atan2(presenter.getInput().getMouseY() - py, presenter.getInput().getMouseX() - px);
                Graphics2D gA = (Graphics2D) g2.create();
                gA.translate(px, py);
                gA.rotate(angle);
                gA.drawImage(aimArrowImg, 35, -15, 30, 30, null);
                gA.dispose();
            }

            drawPenguin(g);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        // Rendering HUD and screen dimming for non-active states
        hud.draw(g, presenter.getPlayer(), customFont, playerBarImg, skill1Img, skill2Img, skill3Img, ballPImg);

        if (presenter.getInput().isPaused() || !presenter.getPlayer().isAlive()) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, 800, 600);
        }
    }

    /**
     * Extracts and draws the appropriate frame from the player's sprite sheet
     * based on movement direction and animation step.
     */
    private void drawPenguin(Graphics g) {
        if (penguinSheet == null)
            return;
        int row = 0;
        InputHandler in = presenter.getInput();
        if (in.isLeft())
            row = 1;
        else if (in.isRight())
            row = 2;
        else if (in.isUp())
            row = 3;
        else if (in.isDown())
            row = 0;
        spriteCounter++;
        if (spriteCounter > 10) {
            spriteNum = (spriteNum + 1) % 3;
            spriteCounter = 0;
        }
        int fw = penguinSheet.getWidth() / 3;
        int fh = penguinSheet.getHeight() / 4;
        BufferedImage sub = penguinSheet.getSubimage(spriteNum * fw, row * fh, fw, fh);
        g.drawImage(sub, presenter.getPlayer().getX(), presenter.getPlayer().getY(), 50, 50, null);
    }
}