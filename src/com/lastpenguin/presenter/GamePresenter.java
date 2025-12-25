/**
 * Copyright (c) 2025 Muhammad 'Azmi Salam. All Rights Reserved.
 */
package com.lastpenguin.presenter;

import com.lastpenguin.model.*;
import com.lastpenguin.view.GamePanel;
import java.util.*;
import javax.swing.Timer;
import java.awt.Rectangle;

public class GamePresenter {
    private Player player;
    private GameSettings settings;
    private List<Yeti> yetis = new ArrayList<>();
    private List<Projectile> projectiles = new ArrayList<>();
    private List<Obstacle> obstacles = new ArrayList<>();
    private InputHandler input;
    private GamePanel view;
    private Random rand = new Random();
    private int spawnTimer = 0;
    private int shootCooldown = 0;
    private Runnable onGameOverCallback;

    public GamePresenter(Player player, GamePanel view, GameSettings settings, Runnable onGameOver) {
        this.player = player;
        this.view = view;
        this.settings = settings;
        this.input = new InputHandler();
        this.view.addKeyListener(input);
        this.onGameOverCallback = onGameOver; // Simpan callback dari Main
        spawnObstacles();
    }

    /**
     * Spawns obstacles randomly while ensuring they don't overlap with the player's start.
     */
    private void spawnObstacles() {
        Rectangle playerSafeZone = new Rectangle(player.getX() - 50, player.getY() - 50, 150, 150);
        for(int i = 0; i < 5; i++) {
            int ox, oy;
            Rectangle obsRect;
            do {
                ox = rand.nextInt(600) + 100;
                oy = rand.nextInt(350) + 50;
                obsRect = new Rectangle(ox, oy, 80, 80);
            } while (obsRect.intersects(playerSafeZone)); // Retry if spawning on player
            
            obstacles.add(new Obstacle(ox, oy, 80, 80));
        }
    }

    public void update() {
        if (!player.isAlive()) {
            // Jika sudah mati, tunggu sebentar lalu panggil callback untuk kembali ke menu
            // Kita beri delay sedikit agar pemain bisa melihat tulisan "GAME OVER"
            new javax.swing.Timer(2000, e -> {
                ((javax.swing.Timer)e.getSource()).stop();
                if (onGameOverCallback != null) onGameOverCallback.run();
            }).start();
            return; 
        }
        
        if (input.isPaused()) return;

        handleMovement();
        handleCombat();
        checkCollisions();
        
        // Logika Difficulty (Medium/Hard)
        int spawnRate = 240; 
        int diffVal = 1;
        if (settings.getDifficulty().equals(GameSettings.MEDIUM)) {
            spawnRate = 120; diffVal = 2;
        } else if (settings.getDifficulty().equals(GameSettings.HARD)) {
            spawnRate = 60; diffVal = 3;
        }

        spawnTimer++;
        if (spawnTimer > spawnRate) {
            yetis.add(new Yeti(rand.nextInt(700), 580, diffVal)); 
            spawnTimer = 0;
        }
        view.repaint();
    }

    private void handleMovement() {
        int dx = 0, dy = 0;
        if (input.isUp()) dy--; if (input.isDown()) dy++;
        if (input.isLeft()) dx--; if (input.isRight()) dx++;
        
        if (dx == 0 && dy == 0) return;

        // Player vs Obstacle Collision
        Rectangle nextBounds = new Rectangle(player.getX() + dx * 5, player.getY() + dy * 5, 40, 40);
        boolean blocked = false;
        for (Obstacle o : obstacles) {
            if (nextBounds.intersects(new Rectangle(o.getX(), o.getY(), o.getWidth(), o.getHeight()))) {
                blocked = true; break;
            }
        }
        if (!blocked) player.move(dx, dy);

        // Yeti vs Obstacle Collision
        for (Yeti y : yetis) {
            int oldX = y.getX();
            int oldY = y.getY();
            y.trackPlayer(player.getX(), player.getY());
            
            for (Obstacle o : obstacles) {
                if (y.getBounds().intersects(new Rectangle(o.getX(), o.getY(), o.getWidth(), o.getHeight()))) {
                    // Simple collision response: revert move if blocked
                    y.moveBack(y.getX() - oldX, y.getY() - oldY);
                    break;
                }
            }
        }
    }

    private void handleCombat() {
        // Player shooting based on last move direction (X key)
        if (input.isShooting() && player.getRemainingBullets() > 0 && shootCooldown == 0) {
            projectiles.add(new Projectile(player.getX() + 15, player.getY() + 15, 
                                          player.getLastDx(), player.getLastDy(), Projectile.PLAYER_TYPE));
            player.useBullet();
            shootCooldown = 20;
        }
        if (shootCooldown > 0) shootCooldown--;

        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.update();
            if (!p.isActive()) {
                if (p.getOwner().equals(Projectile.YETI_TYPE)) player.addBullets(1);
                it.remove(); continue;
            }
            // Projectile vs Obstacle
            for (Obstacle o : obstacles) {
                if (p.getBounds().intersects(new Rectangle(o.getX(), o.getY(), o.getWidth(), o.getHeight()))) {
                    p.setActive(false); break;
                }
            }
        }

        // Yeti Shooting Aggression based on Difficulty
        int shootChance = settings.getDifficulty().equals(GameSettings.EASY) ? 300 : 150;
        for (Yeti y : yetis) {
            if (rand.nextInt(shootChance) < 2) {
                int vx = (player.getX() > y.getX()) ? 1 : -1;
                int vy = (player.getY() > y.getY()) ? -1 : 1;
                projectiles.add(new Projectile(y.getX() + 30, y.getY() + 30, vx, vy, Projectile.YETI_TYPE));
            }
        }
    }

    private void checkCollisions() {
        Rectangle pBounds = player.getBounds();
        for (Yeti y : yetis) {
            if (pBounds.intersects(y.getBounds())) { player.die(); return; }
        }
        for (Projectile p : projectiles) {
            if (p.getOwner().equals(Projectile.YETI_TYPE)) {
                if (p.getBounds().intersects(pBounds)) player.die();
            } else {
                for (Yeti y : yetis) {
                    if (p.getBounds().intersects(y.getBounds())) {
                        y.takeDamage(50); p.setActive(false);
                        if (!y.isAlive()) player.registerKill(100);
                    }
                }
            }
        }
        yetis.removeIf(y -> !y.isAlive());
    }

    public List<Obstacle> getObstacles() { return obstacles; }
    public List<Yeti> getYetis() { return yetis; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public Player getPlayer() { return player; }
    public InputHandler getInput() { return input; }
    public void startGame() { new Timer(16, e -> update()).start(); }
}