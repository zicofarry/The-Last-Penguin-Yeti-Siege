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
    
    private Timer gameLoop; // Referensi timer untuk menghentikan loop saat mati
    private Runnable onGameOverCallback;
    private boolean isGameOverCalled = false; // Flag untuk mencegah duplikasi simpan skor

    public GamePresenter(Player player, GamePanel view, GameSettings settings, Runnable onGameOver) {
        this.player = player;
        this.view = view;
        this.settings = settings;
        this.input = new InputHandler();
        this.view.addKeyListener(input);
        this.onGameOverCallback = onGameOver;
        spawnObstacles();
    }

    private void spawnObstacles() {
        Rectangle playerSafeZone = new Rectangle(player.getX() - 50, player.getY() - 50, 150, 150);
        for(int i = 0; i < 5; i++) {
            int ox, oy;
            Rectangle obsRect;
            do {
                ox = rand.nextInt(600) + 100;
                oy = rand.nextInt(350) + 50;
                obsRect = new Rectangle(ox, oy, 80, 80);
            } while (obsRect.intersects(playerSafeZone));
            
            obstacles.add(new Obstacle(ox, oy, 80, 80));
        }
    }

    public void update() {
        // Logika Game Over
        if (!player.isAlive()) {
            if (!isGameOverCalled) {
                isGameOverCalled = true; // Kunci agar proses ini hanya jalan 1x
                if (gameLoop != null) gameLoop.stop(); // Hentikan loop game segera!

                // Beri jeda 2 detik agar pemain melihat tulisan "GAME OVER" sebelum pindah
                Timer delayTimer = new Timer(2000, e -> {
                    ((Timer)e.getSource()).stop();
                    if (onGameOverCallback != null) onGameOverCallback.run();
                });
                delayTimer.setRepeats(false);
                delayTimer.start();
            }
            return; 
        }
        
        if (input.isPaused()) return;

        handleMovement();
        handleCombat();
        checkCollisions();
        
        // Logika Difficulty Terintegrasi
        int spawnRate = 240; // Default Easy
        int diffVal = 1;     // Kecepatan Yeti (Easy)

        if (settings.getDifficulty().equals(GameSettings.MEDIUM)) {
            spawnRate = 120; // Medium: Spawn lebih cepat
            diffVal = 2;     // Medium: Yeti lebih cepat
        } else if (settings.getDifficulty().equals(GameSettings.HARD)) {
            spawnRate = 80;  // Hard: Spawn sangat cepat
            diffVal = 3;     // Hard: Yeti sangat agresif/cepat
        }

        spawnTimer++;
        if (spawnTimer > spawnRate) {
            yetis.add(new Yeti(rand.nextInt(700), 580, diffVal)); 
            spawnTimer = 0;
        }
        view.repaint();
    }

    // Bagian handleMovement yang sudah diperbaiki
    private void handleMovement() {
        int dx = 0, dy = 0;
        if (input.isUp()) dy--; if (input.isDown()) dy++;
        if (input.isLeft()) dx--; if (input.isRight()) dx++;
        
        // 1. Player Movement (Hanya jika ada input)
        if (dx != 0 || dy != 0) {
            Rectangle nextBounds = new Rectangle(player.getX() + dx * 5, player.getY() + dy * 5, 40, 40);
            boolean blocked = false;
            for (Obstacle o : obstacles) {
                if (nextBounds.intersects(new Rectangle(o.getX(), o.getY(), o.getWidth(), o.getHeight()))) {
                    blocked = true; break;
                }
            }
            if (!blocked) player.move(dx, dy);
        }

        // 2. Yeti Movement (Yeti TERUS BERGERAK mencari player meskipun player diam)
        for (Yeti y : yetis) {
            int oldX = y.getX();
            int oldY = y.getY();
            y.trackPlayer(player.getX(), player.getY());
            
            // Cek tabrakan Yeti dengan Obstacle
            for (Obstacle o : obstacles) {
                if (y.getBounds().intersects(new Rectangle(o.getX(), o.getY(), o.getWidth(), o.getHeight()))) {
                    y.moveBack(y.getX() - oldX, y.getY() - oldY); // Yeti tidak bisa tembus batu
                    break;
                }
            }
        }
    }

    private void handleCombat() {
        // Tembakan Player (Kecepatan tetap 8)
        if (input.isShooting() && player.getRemainingBullets() > 0 && shootCooldown == 0) {
            projectiles.add(new Projectile(player.getX() + 15, player.getY() + 15, 
                        player.getLastDx(), player.getLastDy(), Projectile.PLAYER_TYPE, 8));
            player.useBullet();
            shootCooldown = 20;
        }
        if (shootCooldown > 0) shootCooldown--;

        // Tembakan Yeti (Presisi & Difficulty Speed)
        int shootChance = settings.getDifficulty().equals(GameSettings.EASY) ? 400 : 200;
        int bulletSpeed = 8; // Default Medium (Sama dengan player)
        
        if (settings.getDifficulty().equals(GameSettings.EASY)) bulletSpeed = 5;
        else if (settings.getDifficulty().equals(GameSettings.HARD)) bulletSpeed = 12;

        for (Yeti y : yetis) {
            if (rand.nextInt(shootChance) < 2) {
                // Hitung arah presisi ke Player (Targeting pusat player)
                double targetX = player.getX() + 20;
                double targetY = player.getY() + 20;
                double diffX = targetX - (y.getX() + 30);
                double diffY = targetY - (y.getY() + 30);
                
                projectiles.add(new Projectile(y.getX() + 30, y.getY() + 30, diffX, diffY, Projectile.YETI_TYPE, bulletSpeed));
            }
        }

        // Update Projectiles & Collision dengan Obstacle
        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.update();
            
            // SEMUA Peluru hancur jika menabrak Obstacle
            boolean hitObstacle = false;
            for (Obstacle o : obstacles) {
                if (p.getBounds().intersects(new Rectangle(o.getX(), o.getY(), o.getWidth(), o.getHeight()))) {
                    p.setActive(false);
                    hitObstacle = true;
                    break;
                }
            }

            if (!p.isActive()) {
                if (p.getOwner().equals(Projectile.PLAYER_TYPE) && !p.isHit()) player.registerMiss();
                if (p.getOwner().equals(Projectile.YETI_TYPE)) player.addBullets(1);
                it.remove();
            }
        }
    }

    private void checkCollisions() {
        Rectangle pBounds = player.getBounds();
        for (Projectile p : projectiles) {
            if (!p.getOwner().equals(Projectile.PLAYER_TYPE)) {
                if (p.getBounds().intersects(pBounds)) player.die();
            } else {
                for (Yeti y : yetis) {
                    if (p.getBounds().intersects(y.getBounds())) {
                        y.takeDamage(50);
                        p.setActive(false);
                        p.setHit(true); // TANDAI SEBAGAI HIT
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
    
    public void startGame() { 
        gameLoop = new Timer(16, e -> update());
        gameLoop.start(); 
    }
}