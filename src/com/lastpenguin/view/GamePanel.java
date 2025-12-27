package com.lastpenguin.view;

import com.lastpenguin.model.*;
import com.lastpenguin.presenter.GamePresenter;
import com.lastpenguin.presenter.InputHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;

public class GamePanel extends JPanel {
    private GamePresenter presenter;
    private HUD hud = new HUD();
    private BufferedImage arenaImg, penguinSheet, obstacleImg, lubangImg;
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

        // --- 1. SETUP PAUSE MENU (Papan Tengah 520x650) ---
        int pauseW = 520;
        int pauseH = 650;
        int pauseX = (800 - pauseW) / 2;
        int pauseY = (600 - pauseH) / 2;
        
        pauseMenu = createOverlayPanel(pauseX, pauseY, pauseW, pauseH, pauseBgImage);
        lblPauseStats = createStatsLabel(pauseW, 32f); 
        lblPauseStats.setForeground(new Color(60, 40, 20)); // Cokelat Kayu untuk Pause
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

        // Tombol Surrender: Sekarang TIDAK memanggil quitAction
        pauseMenu.add(createStyledPauseButton("SURRENDER", 280, pBtnY, pBtnW, 40, e -> {
            soundManager.playEffect("sfx_click.wav");
            if (presenter != null) {
                presenter.surrender(); // Buat player mati (isAlive = false)
                presenter.getInput().setPaused(false); // Unpause agar presenter.update mendeteksi kematian
                updatePauseUI(false); // Langsung pindah ke tampilan Game Over
            }
        }));
        add(pauseMenu);

        // --- 2. SETUP GAME OVER MENU (Full Screen 800x600) ---
        gameOverMenu = createOverlayPanel(0, 0, 800, 600, gameOverBgImage);
        
        lblGameOverStats = createStatsLabel(800, 40f); 
        // PERBAIKAN: Warna teks dibuat cerah (Putih Es/Cyan) untuk background gelap
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
            quitAction.actionPerformed(e); // Ini baru kembali ke Main Menu
        }));
        add(gameOverMenu);
    }

    private JPanel createOverlayPanel(int x, int y, int w, int h, Image bgImage) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                if (bgImage != null) g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
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
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setForeground(new Color(100, 150, 255)); } 
            @Override
            public void mouseExited(MouseEvent e) { btn.setForeground(new Color(60, 40, 20)); }
        });
        return btn;
    }

    private JButton createStyledGameOverButton(String text, int x, int y, int w, int h, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(customFont.deriveFont(Font.BOLD, 18f));
        btn.setForeground(new Color(255, 255, 255)); 
        btn.setBounds(x, y, w, h);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setForeground(new Color(255, 200, 0)); } 
            @Override
            public void mouseExited(MouseEvent e) { btn.setForeground(Color.WHITE); }
        });
        return btn;
    }

    public void updatePauseUI(boolean isPaused) {
        if (presenter != null && !presenter.getPlayer().isAlive()) {
            pauseMenu.setVisible(false); 
            if (!gameOverMenu.isVisible()) {
                Player p = presenter.getPlayer();
                // Menggunakan warna cerah melalui HTML style jika perlu, atau cukup lewat setForeground
                lblGameOverStats.setText("<html><div style='text-align: center;'>" +
                    "FINAL SCORE<br>" + p.getScore() + "</div></html>");
                gameOverMenu.setVisible(true);
            }
            return;
        }

        if (isPaused) {
            if (!pauseMenu.isVisible()) {
                Player p = presenter.getPlayer();
                lblPauseStats.setText("<html><div style='text-align: center;'>" +
                    "SCORE: " + p.getScore() + "<br>" +
                    "BULLETS: " + p.getRemainingBullets() + "<br>" +
                    "MISSES: " + p.getMissedShots() + "<br>" +
                    "YETIS KILLED: " + p.getYetiKilled() + "</div></html>");
                pauseMenu.setVisible(true);
            }
        } else {
            pauseMenu.setVisible(false);
        }
        
        if (presenter != null && presenter.getPlayer().isAlive()) {
            gameOverMenu.setVisible(false);
        }
    }

    private void loadAssets() {
        arenaImg = AssetLoader.loadImage("arena.png");
        penguinSheet = AssetLoader.loadImage("penguin.png");
        obstacleImg = AssetLoader.loadImage("obstacle.png");
        lubangImg = AssetLoader.loadImage("lubang.png");
        yetiSprites = AssetLoader.loadYetiSprites();
        pauseBgImage = AssetLoader.loadImage("ui/pause_bg.png");
        gameOverBgImage = AssetLoader.loadImage("ui/game_over.png");
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/assets/fonts/icy_font.ttf")).deriveFont(18f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (Exception e) {
            customFont = new Font("Arial", Font.BOLD, 18);
        }
    }

    public void setPresenter(GamePresenter p) { this.presenter = p; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (presenter == null) return;
        Graphics2D g2 = (Graphics2D) g;

        if (arenaImg != null) g.drawImage(arenaImg, 0, 0, 800, 600, null);

        for (Obstacle o : presenter.getObstacles()) {
            if (o.isHole()) {
                g.drawImage(lubangImg, o.getX(), o.getY(), o.getWidth(), o.getHeight(), null);
            } else {
                int padding = 15;
                g.drawImage(obstacleImg, o.getX() - padding, o.getY() - padding, 
                            o.getWidth() + (padding*2), o.getHeight() + (padding*2), null);
            }
        }

        for (Projectile p : presenter.getProjectiles()) {
            g.setColor(p.getOwner().equals(Projectile.YETI_TYPE) ? Color.CYAN : Color.YELLOW);
            int size = p.isPiercing() ? 40 : 12;
            g.fillOval(p.getX(), p.getY(), size, size);
        }

        for (Yeti y : presenter.getYetis()) {
            int index = y.getSpriteIndex();
            if (yetiSprites != null && index < yetiSprites.length) {
                g.drawImage(yetiSprites[index], y.getX(), y.getY(), 75, 75, null);
            }
        }

        if (presenter.getPlayer().isAlive()) {
            if (presenter.getPlayer().isGhost()) g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            drawPenguin(g);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        hud.draw(g, presenter.getPlayer());

        if (presenter.getInput().isPaused() || !presenter.getPlayer().isAlive()) {
            g.setColor(new Color(0, 0, 0, 150)); 
            g.fillRect(0, 0, 800, 600);
        }
    }

    private void drawPenguin(Graphics g) {
        if (penguinSheet == null) return;
        int row = 0;
        InputHandler in = presenter.getInput();
        if (in.isLeft()) row = 1; else if (in.isRight()) row = 2;
        else if (in.isUp()) row = 3; else if (in.isDown()) row = 0;
        spriteCounter++;
        if (spriteCounter > 10) { spriteNum = (spriteNum + 1) % 3; spriteCounter = 0; }
        int fw = penguinSheet.getWidth() / 3;
        int fh = penguinSheet.getHeight() / 4;
        BufferedImage sub = penguinSheet.getSubimage(spriteNum * fw, row * fh, fw, fh);
        g.drawImage(sub, presenter.getPlayer().getX(), presenter.getPlayer().getY(), 50, 50, null);
    }
}