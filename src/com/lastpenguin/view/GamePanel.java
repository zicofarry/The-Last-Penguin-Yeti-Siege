package com.lastpenguin.view;

import com.lastpenguin.model.*;
import com.lastpenguin.presenter.GamePresenter;
import com.lastpenguin.presenter.InputHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class GamePanel extends JPanel {
    private GamePresenter presenter;
    private HUD hud = new HUD();
    private BufferedImage arenaImg, penguinSheet, obstacleImg, lubangImg;
    private int spriteNum = 0, spriteCounter = 0;
    private JPanel pauseMenu;
    private JLabel lblStats;
    private BufferedImage[] yetiSprites;

    public GamePanel(ActionListener quitAction) { // Lepas resumeAction dari constructor jika hanya untuk unpause
        setPreferredSize(new Dimension(800, 600));
        setDoubleBuffered(true);
        setFocusable(true);
        setLayout(null); 
        loadAssets();

        // Inisialisasi Menu Pause
        pauseMenu = new JPanel();
        pauseMenu.setBounds(250, 100, 300, 400);
        pauseMenu.setBackground(new Color(0, 0, 0, 220));
        pauseMenu.setLayout(new BoxLayout(pauseMenu, BoxLayout.Y_AXIS));
        pauseMenu.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        pauseMenu.setVisible(false);

        JLabel pauseLabel = new JLabel("--- PAUSED ---", SwingConstants.CENTER);
        pauseLabel.setForeground(Color.WHITE);
        pauseLabel.setFont(new Font("Arial", Font.BOLD, 24));
        pauseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblStats = new JLabel("", SwingConstants.CENTER);
        lblStats.setForeground(Color.CYAN);
        lblStats.setFont(new Font("Consolas", Font.PLAIN, 14));
        lblStats.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnResume = new JButton("RESUME GAME");
        btnResume.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnResume.addActionListener(e -> {
            if (presenter != null) {
                presenter.getInput().setPaused(false);
                this.requestFocusInWindow();
            }
        });

        JButton btnSurrender = new JButton("SURRENDER & QUIT");
        btnSurrender.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSurrender.addActionListener(e -> {
            if (presenter != null) presenter.surrender();
            quitAction.actionPerformed(e);
        });

        pauseMenu.add(Box.createRigidArea(new Dimension(0, 20)));
        pauseMenu.add(pauseLabel);
        pauseMenu.add(Box.createRigidArea(new Dimension(0, 20)));
        pauseMenu.add(lblStats);
        pauseMenu.add(Box.createRigidArea(new Dimension(0, 30)));
        pauseMenu.add(btnResume);
        pauseMenu.add(Box.createRigidArea(new Dimension(0, 10)));
        pauseMenu.add(btnSurrender);
        
        add(pauseMenu);
    }

    private void loadAssets() {
        arenaImg = AssetLoader.loadImage("arena.png");
        penguinSheet = AssetLoader.loadImage("penguin.png");
        obstacleImg = AssetLoader.loadImage("obstacle.png");
        lubangImg = AssetLoader.loadImage("lubang.png");
        yetiSprites = AssetLoader.loadYetiSprites();
    }

    public void setPresenter(GamePresenter p) { this.presenter = p; }

    // Method baru untuk update UI Pause secara aman (dipanggil dari Presenter)
    public void updatePauseUI(boolean isPaused) {
        if (isPaused && !pauseMenu.isVisible()) {
            Player p = presenter.getPlayer();
            String statsText = "<html><div style='text-align: center; color: white;'>" +
                "PLAYER: " + p.getUsername() + "<br><br>" +
                "SCORE: " + p.getScore() + "<br>" +
                "BULLETS: " + p.getRemainingBullets() + "<br>" +
                "KILLS: " + p.getYetiKilled() + "<br>" +
                "MISSES: " + p.getMissedShots() + "</div></html>";
            lblStats.setText(statsText);
            pauseMenu.setVisible(true);
        } else if (!isPaused && pauseMenu.isVisible()) {
            pauseMenu.setVisible(false);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (presenter == null) return;
        Graphics2D g2 = (Graphics2D) g;

        if (arenaImg != null) g.drawImage(arenaImg, 0, 0, 800, 600, null);

        for (Obstacle o : presenter.getObstacles()) {
            if (o.isHole()) g.drawImage(lubangImg, o.getX(), o.getY(), o.getWidth(), o.getHeight(), null);
            else g.drawImage(obstacleImg, o.getX(), o.getY(), o.getWidth(), o.getHeight(), null);
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
        } else {
            g.setColor(new Color(255, 0, 0, 150));
            g.fillRect(0, 0, 800, 600);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("GAME OVER", 250, 300);
        }

        hud.draw(g, presenter.getPlayer());

        if (presenter.getInput().isPaused()) {
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