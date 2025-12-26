package com.lastpenguin.view;

import com.lastpenguin.model.*;
import com.lastpenguin.presenter.GamePresenter;
import com.lastpenguin.presenter.InputHandler;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class GamePanel extends JPanel {
    private GamePresenter presenter;
    private HUD hud = new HUD();
    private BufferedImage arenaImg, penguinSheet, yetiSheet, obstacleImg;
    private int spriteNum = 0, spriteCounter = 0;
    private JPanel pauseMenu;
    private BufferedImage[] yetiSprites = AssetLoader.loadYetiSprites();

    public GamePanel(ActionListener resumeAction, ActionListener quitAction) {
        setPreferredSize(new Dimension(800, 600));
        setDoubleBuffered(true);
        setFocusable(true);
        setLayout(null); // Gunakan null agar bisa menaruh menu pause di tengah
        loadAssets();

        // Inisialisasi Menu Pause
        pauseMenu = new JPanel();
        pauseMenu.setBounds(300, 200, 200, 200);
        pauseMenu.setBackground(new Color(0, 0, 0, 180));
        pauseMenu.setLayout(new GridLayout(3, 1, 10, 10));
        pauseMenu.setVisible(false); // Sembunyi di awal

        JButton btnResume = new JButton("RESUME");
        JButton btnQuit = new JButton("QUIT TO MENU");

        btnResume.addActionListener(resumeAction);
        btnQuit.addActionListener(quitAction);

        pauseMenu.add(new JLabel("PAUSED", SwingConstants.CENTER));
        pauseMenu.add(btnResume);
        pauseMenu.add(btnQuit);
        add(pauseMenu);
    }


    private void loadAssets() {
        arenaImg = AssetLoader.loadImage("arena.png");
        penguinSheet = AssetLoader.loadImage("penguin.png");
        obstacleImg = AssetLoader.loadImage("obstacle.png");
        yetiSprites = AssetLoader.loadYetiSprites(); // Load 12 frame yeti
    }

    public void setPresenter(GamePresenter p) { this.presenter = p; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (presenter == null) return;

        if (arenaImg != null) g.drawImage(arenaImg, 0, 0, 800, 600, null);

        for (Obstacle o : presenter.getObstacles()) 
            g.drawImage(obstacleImg, o.getX(), o.getY(), o.getWidth(), o.getHeight(), null);

        for (Projectile p : presenter.getProjectiles()) {
            g.setColor(p.getOwner().equals(Projectile.YETI_TYPE) ? Color.CYAN : Color.YELLOW);
            g.fillOval(p.getX(), p.getY(), 12, 12);
        }

        for (Yeti y : presenter.getYetis()) {
            int index = y.getSpriteIndex();
            if (yetiSprites != null && index < yetiSprites.length) {
                g.drawImage(yetiSprites[index], y.getX(), y.getY(), 75, 75, null);
            }
        }

        if (presenter.getPlayer().isAlive()) {
            drawPenguin(g);
        } else {
            g.setColor(new Color(255, 0, 0, 150));
            g.fillRect(0, 0, 800, 600);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("GAME OVER", 250, 300);
        }

        hud.draw(g, presenter.getPlayer());

        if (presenter.getInput().isPaused()) {
            g.setColor(new Color(0,0,0,150)); g.fillRect(0,0,800,600);
            g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("PAUSED", 300, 300);
        }
        if (presenter.getInput().isPaused()) {
            pauseMenu.setVisible(true);
        } else {
            pauseMenu.setVisible(false);
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

    // private void drawYeti(Graphics2D g2, Yeti yeti) {
    //     int spriteIndex = yeti.getSpriteIndex();
    //     BufferedImage image = yetiSprites[spriteIndex];
        
    //     g2.drawImage(image, yeti.getX(), yeti.getY(), 64, 64, null);
    // }
}