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
    
    private Timer gameLoop;
    private Runnable onGameOverCallback;
    private boolean isGameOverCalled = false;

    public GamePresenter(Player player, GamePanel view, GameSettings settings, Runnable onGameOver) {
        this.player = player;
        this.view = view;
        this.settings = settings;
        this.input = new InputHandler();
        
        // PENTING: Tambahkan Listener Mouse ke view
        this.view.addKeyListener(input);
        this.view.addMouseListener(input);
        this.view.addMouseMotionListener(input);
        
        this.onGameOverCallback = onGameOver;
        spawnInitialObstacles();
        this.input.setSettings(settings);
    }

    private void spawnInitialObstacles() {
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
        if (!player.isAlive()) {
            if (!isGameOverCalled) {
                isGameOverCalled = true;
                if (gameLoop != null) gameLoop.stop();
                Timer delayTimer = new Timer(2000, e -> {
                    ((Timer)e.getSource()).stop();
                    if (onGameOverCallback != null) onGameOverCallback.run();
                });
                delayTimer.setRepeats(false);
                delayTimer.start();
            }
            return; 
        }
        
        // Sinkronkan UI Pause dengan state input
        view.updatePauseUI(input.isPaused());

        if (input.isPaused()) { 
            view.repaint(); 
            return; 
        }
        
        player.updateTimers();
        handleSkills();
        handleMovement();
        handleCombat();
        checkCollisions();
        
        obstacles.removeIf(o -> {
            o.update();
            return o.isExpired();
        });
        
        // Spawning Yeti logic...
        int spawnRate = settings.getDifficulty().equals(GameSettings.HARD) ? 80 : 
                        settings.getDifficulty().equals(GameSettings.MEDIUM) ? 120 : 200;
        spawnTimer++;
        if (spawnTimer > spawnRate) {
            yetis.add(new Yeti(rand.nextInt(700), 580, settings.getDifficulty().equals(GameSettings.HARD) ? 3 : 1)); 
            spawnTimer = 0;
        }
        view.repaint();
    }

    private void handleMovement() {
        int dx = 0, dy = 0;
        if (input.isUp()) dy--; if (input.isDown()) dy++;
        if (input.isLeft()) dx--; if (input.isRight()) dx++;
        
        if (dx != 0 || dy != 0) {
            Rectangle currentBounds = player.getBounds();
            Rectangle nextBounds = new Rectangle(player.getX() + dx * 5, player.getY() + dy * 5, 40, 40);
            boolean blocked = false;

            for (Obstacle o : obstacles) {
                Rectangle obsBounds = new Rectangle(o.getX(), o.getY(), o.getWidth(), o.getHeight());
                if (nextBounds.intersects(obsBounds)) {
                    // LOGIKA ANTI-STUCK: Jika itu lubang, hanya blokir jika pemain sebelumnya di luar lubang
                    if (o.isHole()) {
                        if (!currentBounds.intersects(obsBounds)) { blocked = true; break; }
                    } else {
                        blocked = true; break;
                    }
                }
            }
            if (!blocked) player.move(dx, dy);
        }

        for (Yeti y : yetis) {
            int oldX = y.getX(); int oldY = y.getY();
            
            // Skill 3: Jika Ghost aktif, Yeti tidak bisa melihat posisi Penguin (gerak acak)
            if (player.isGhost()) y.trackPlayer(rand.nextInt(800), rand.nextInt(600));
            else y.trackPlayer(player.getX(), player.getY());
            
            for (Obstacle o : obstacles) {
                if (y.getBounds().intersects(new Rectangle(o.getX(), o.getY(), o.getWidth(), o.getHeight()))) {
                    y.moveBack(y.getX() - oldX, y.getY() - oldY);
                    break;
                }
            }
        }
    }

    private void handleSkills() {
        // SKILL 1: Giant Snowball
        if (input.isS1() && player.getCooldownS1() == 0 && player.getRemainingBullets() >= 5) {
            player.addBullets(-5);
            player.setS1RemainingShots(3);
            player.setCooldownS1(600); 
        }

        // SKILL 2: Meteor
        if (input.isS2() && player.getCooldownS2() == 0 && player.getRemainingBullets() >= 10) {
            player.addBullets(-10);
            // Jika mouse diklik, meteor jatuh di mouse, jika tidak di posisi penguin
            int tx = (input.getMouseX() > 0) ? input.getMouseX() - 40 : player.getX();
            int ty = (input.getMouseY() > 0) ? input.getMouseY() - 40 : player.getY();
            
            yetis.removeIf(y -> {
                double dist = Math.sqrt(Math.pow(y.getX() - tx, 2) + Math.pow(y.getY() - ty, 2));
                if (dist < 200) { player.registerKill(100); return true; }
                return false;
            });
            
            obstacles.add(new Obstacle(tx, ty, 80, 80, true, 300)); 
            player.setCooldownS2(900); 
        }

        // SKILL 3: Ghost
        if (input.isS3() && player.getCooldownS3() == 0 && player.getRemainingBullets() >= 3) {
            player.addBullets(-3);
            player.setGhostDuration(300); 
            player.setCooldownS3(600); 
        }
    }

    private void handleCombat() {
        boolean wantToShoot = input.isShooting() || (settings.isUseMouse() && input.isMouseClicked());
        if (wantToShoot && player.getRemainingBullets() > 0 && shootCooldown == 0) {
            double targetDx, targetDy;
           if (settings.isUseMouse() && input.isMouseClicked()) {
                targetDx = input.getMouseX() - (player.getX() + 25);
                targetDy = input.getMouseY() - (player.getY() + 25);
            } else {
                targetDx = player.getLastDx(); 
                targetDy = player.getLastDy();
            }

            Projectile p = new Projectile(player.getX()+15, player.getY()+15, targetDx, targetDy, Projectile.PLAYER_TYPE, 8);
            
            if (player.getS1RemainingShots() > 0) {
                p.setPiercing(true); // Skill 1 aktif
                player.useS1Shot();
            }
            
            projectiles.add(p);
            player.useBullet();
            shootCooldown = 15;
        }
        if (shootCooldown > 0) shootCooldown--;

        // Tembakan Yeti
        for (Yeti y : yetis) {
            if (rand.nextInt(300) < 2) {
                projectiles.add(new Projectile(y.getX()+30, y.getY()+30, player.getX()-y.getX(), player.getY()-y.getY(), Projectile.YETI_TYPE, 6));
            }
        }

        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.update();
            
            // Skill 1: Giant Snowball menembus obstacle
            if (!p.isPiercing()) {
                for (Obstacle o : obstacles) {
                    if (!o.isHole() && p.getBounds().intersects(new Rectangle(o.getX(), o.getY(), o.getWidth(), o.getHeight()))) {
                        p.setActive(false); break;
                    }
                }
            }

            // --- BAGIAN LOGIKA REFILL PELURU (DIPERBAIKI) ---
            if (!p.isActive() || p.getX() < 0 || p.getX() > 800 || p.getY() < 0 || p.getY() > 600) {
                // Jika peluru Yeti hilang (kena batu atau keluar layar), player dapat peluru
                if (p.getOwner().equals(Projectile.YETI_TYPE)) {
                    player.addBullets(1);
                } 
                // Jika peluru Player hilang dan belum mengenai musuh, catat sebagai Miss
                else if (p.getOwner().equals(Projectile.PLAYER_TYPE) && !p.isHit()) {
                    player.registerMiss();
                }
                it.remove();
            }
        }
    }

    private void checkCollisions() {
        Rectangle pBounds = player.getBounds();
        for (Projectile p : projectiles) {
            if (p.getOwner().equals(Projectile.YETI_TYPE)) {
                // Skill 3: Peluru yeti tembus penguin saat Ghost
                if (!player.isGhost() && p.getBounds().intersects(pBounds)) player.die();
            } else {
                for (Yeti y : yetis) {
                    if (p.getBounds().intersects(y.getBounds())) {
                        y.takeDamage(100);
                        p.setHit(true); // TANDAI SUDAH KENA AGAR TIDAK DIANGGAP MISS
                        
                        if (!p.isPiercing()) p.setActive(false); // Peluru biasa hancur, Giant nembus
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
    view.requestFocusInWindow(); // Pastikan fokus saat timer mulai
    gameLoop = new Timer(16, e -> update());
    gameLoop.start(); 
}
    public void surrender() {
        player.die(); // Set status mati agar loop berhenti dan skor tersimpan
        update(); // Panggil update sekali lagi untuk memicu logika Game Over & simpan skor
    }
}