/**
 * Copyright (c) 2025 Muhammad 'Azmi Salam. All Rights Reserved.
 * Email: mhmmdzmslm36@gmail.com
 * GitHub: https://github.com/zicofarry
 */
package com.lastpenguin.presenter;

import com.lastpenguin.model.*;
import com.lastpenguin.view.GamePanel;
import java.util.*;
import javax.swing.Timer;

/**
 * Core game engine logic.
 * Manages interactions between Player, Yeti, and Projectiles.
 */
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

    /**
     * Updated constructor to match Main.java requirements.
     */
    public GamePresenter(Player player, GamePanel view, GameSettings settings) {
        this.player = player;
        this.view = view;
        this.settings = settings;
        this.input = new InputHandler();
        this.view.addKeyListener(input);
        spawnObstacles();
    }

    private void spawnObstacles() {
        for(int i=0; i<4; i++) obstacles.add(new Obstacle(rand.nextInt(600)+100, rand.nextInt(200)+50, 80, 80));
    }

    public void update() {
        if (input.isPaused()) return;

        handleMovement();
        handleCombat();
        checkCollisions();
        
        spawnTimer++;
        if (spawnTimer > 180) {
            yetis.add(new Yeti(rand.nextInt(700), 610, 1));
            spawnTimer = 0;
        }
        view.repaint();
    }

    private void handleMovement() {
        int dx = 0, dy = 0;
        if (input.isUp()) dy--; if (input.isDown()) dy++;
        if (input.isLeft()) dx--; if (input.isRight()) dx++;
        player.move(dx, dy);
    }

    private void handleCombat() {
        if (input.isShooting() && player.getRemainingBullets() > 0 && shootCooldown == 0) {
            projectiles.add(new Projectile(player.getX() + 20, player.getY(), Projectile.PLAYER_TYPE));
            player.useBullet();
            shootCooldown = 20;
        }
        if (shootCooldown > 0) shootCooldown--;

        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.update();
            if (!p.isActive()) {
                // Scavenge logic
                if (p.getOwner().equals(Projectile.YETI_TYPE) && p.getY() < 0) {
                    player.addBullets(1);
                } 
                // Tracking missed shots for the player
                else if (p.getOwner().equals(Projectile.PLAYER_TYPE) && p.getY() < 0) {
                    player.registerMiss();
                }
                it.remove();
            }
        }

        for (Yeti y : yetis) {
            if (rand.nextInt(200) < 2) {
                projectiles.add(new Projectile(y.getX() + 30, y.getY(), Projectile.YETI_TYPE));
            }
        }
    }

    private void checkCollisions() {
        for (Projectile p : projectiles) {
            if (p.getOwner().equals(Projectile.PLAYER_TYPE)) {
                for (Yeti y : yetis) {
                    if (Math.abs(p.getX() - y.getX()) < 50 && Math.abs(p.getY() - y.getY()) < 50) {
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