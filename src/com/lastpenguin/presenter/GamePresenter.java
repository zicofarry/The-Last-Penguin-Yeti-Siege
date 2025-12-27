package com.lastpenguin.presenter;

import com.lastpenguin.model.*;
import com.lastpenguin.view.GamePanel;
import java.util.*;
import javax.swing.Timer;
import java.awt.Rectangle;
import com.lastpenguin.view.Sound;

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
    private Sound soundManager = new Sound();

    public GamePresenter(Player player, GamePanel view, GameSettings settings, Runnable onGameOver) {
        this.player = player;
        this.view = view;
        this.settings = settings;
        this.input = new InputHandler();
        
        // Tambahkan Listener ke view
        this.view.addKeyListener(input);
        this.view.addMouseListener(input);
        this.view.addMouseMotionListener(input);
        
        this.onGameOverCallback = onGameOver;
        spawnInitialObstacles(); 
        this.input.setSettings(settings);
        this.soundManager.setSettings(settings);
    }

    /**
     * Menghasilkan satu rintangan acak (Batu 30HP atau Duri 15HP).
     */
    private void spawnOneRandomObstacle() {
        Rectangle playerSafeZone = new Rectangle(player.getX() - 50, player.getY() - 50, 150, 150);
        int ox, oy, initialHp;
        Rectangle obsRect;
        do {
            ox = rand.nextInt(600) + 100;
            oy = rand.nextInt(350) + 50;
            obsRect = new Rectangle(ox, oy, 80, 80);
        } while (obsRect.intersects(playerSafeZone));
        
        // Random 50:50 mulai dari Rock (30 HP) atau Spike (15 HP)
        initialHp = rand.nextBoolean() ? 30 : 15;
        obstacles.add(new Obstacle(ox, oy, 80, 80, initialHp));
    }

    private void spawnInitialObstacles() {
        obstacles.clear();
        for(int i = 0; i < 5; i++) {
            spawnOneRandomObstacle();
        }
    }

    public void update() {
        // 1. SYNC UI OVERLAY
        view.updatePauseUI(input.isPaused());

        // 2. LOGIKA GAME OVER
        if (!player.isAlive()) {
            if (!isGameOverCalled) {
                isGameOverCalled = true;
                soundManager.playEffect("sfx_game_over.wav");
                if (onGameOverCallback != null) onGameOverCallback.run();
            }
            view.repaint();
            return; 
        }
        
        // 3. LOGIKA PAUSE
        if (input.isPaused()) { 
            view.repaint(); 
            return; 
        }
        
        // 4. LOGIKA GAMEPLAY UTAMA
        player.updateTimers();
        handleSkills();
        handleMovement();
        handleCombat();
        checkCollisions();
        
        // --- UPDATE OBSTACLES DENGAN EFEK SUARA HANCUR ---
        Iterator<Obstacle> obsIt = obstacles.iterator();
        while (obsIt.hasNext()) {
            Obstacle o = obsIt.next();
            o.update();
            
            // Cek jika hancur karena HP habis (Batu/Duri)
            if (o.isDestroyed()) {
                soundManager.playEffect("sfx_ice_break.wav"); // SUARA ES PECAH
                obsIt.remove();
            } 
            // Cek jika menghilang karena durasi habis (Lubang Meteor)
            else if (o.isExpired()) {
                obsIt.remove();
            }
        }

        // Respawn rintangan jika jumlahnya kurang dari 5 (tidak termasuk lubang meteor)
        long currentObsCount = obstacles.stream().filter(o -> !o.isHole()).count();
        while (currentObsCount < 5) {
            spawnOneRandomObstacle();
            currentObsCount++;
        }
        
        // Logika Spawn Yeti
        int spawnRate = settings.getDifficulty().equals(GameSettings.HARD) ? 80 : 
                        settings.getDifficulty().equals(GameSettings.MEDIUM) ? 120 : 200;
        spawnTimer++;
        if (spawnTimer > spawnRate) {
            int yetiHealth = settings.getDifficulty().equals(GameSettings.HARD) ? 3 : 1;
            yetis.add(new Yeti(rand.nextInt(700), 580, yetiHealth)); 
            soundManager.playEffect("sfx_yeti_spawn.wav");
            spawnTimer = 0;
        }

        view.repaint();
    }

    private void handleMovement() {
        // PERGERAKAN PLAYER
        int dx = 0, dy = 0;
        if (input.isUp()) dy--; if (input.isDown()) dy++;
        if (input.isLeft()) dx--; if (input.isRight()) dx++;
        
        if (dx != 0 || dy != 0) {
            if (System.currentTimeMillis() % 350 < 20) {
                soundManager.playEffect("sfx_walk.wav");
            }
            Rectangle currentBounds = player.getBounds();
            Rectangle nextBounds = new Rectangle(player.getX() + dx * 5, player.getY() + dy * 5, 40, 40);
            boolean blocked = false;

            for (Obstacle o : obstacles) {
                Rectangle obsBounds = new Rectangle(o.getX(), o.getY(), o.getWidth(), o.getHeight());
                if (nextBounds.intersects(obsBounds)) {
                    if (o.isHole()) {
                        if (!currentBounds.intersects(obsBounds)) { blocked = true; break; }
                    } else {
                        blocked = true; break;
                    }
                }
            }
            if (!blocked) player.move(dx, dy);
        }

        // PERGERAKAN YETI DENGAN LOGIKA BYPASS
        for (Yeti y : yetis) {
            int oldX = y.getX();
            int oldY = y.getY();
            int targetX = player.isGhost() ? rand.nextInt(800) : player.getX();
            int targetY = player.isGhost() ? rand.nextInt(600) : player.getY();

            int moveDx = (oldX < targetX) ? y.getSpeed() : (oldX > targetX) ? -y.getSpeed() : 0;
            int moveDy = (oldY < targetY) ? y.getSpeed() : (oldY > targetY) ? -y.getSpeed() : 0;

            boolean movedX = false, movedY = false;

            if (moveDx != 0) {
                y.setPosition(oldX + moveDx, oldY);
                if (!isYetiColliding(y)) movedX = true;
                else y.setPosition(oldX, oldY);
            }

            if (moveDy != 0) {
                y.setPosition(y.getX(), oldY + moveDy);
                if (!isYetiColliding(y)) movedY = true;
                else y.setPosition(y.getX(), oldY);
            }

            if (!movedX && !movedY) {
                if (moveDx == 0 && moveDy != 0) { 
                    y.setPosition(oldX + y.getSpeed(), oldY);
                    if (isYetiColliding(y)) y.setPosition(oldX - y.getSpeed(), oldY);
                } 
                else if (moveDy == 0 && moveDx != 0) {
                    y.setPosition(oldX, oldY + y.getSpeed());
                    if (isYetiColliding(y)) y.setPosition(oldX, oldY - y.getSpeed());
                }
            }
            y.updateAnimation(targetX, targetY);
        }
    }

    private boolean isYetiColliding(Yeti y) {
        Rectangle yBounds = y.getBounds();
        for (Obstacle o : obstacles) {
            if (!o.isHole() && yBounds.intersects(new Rectangle(o.getX(), o.getY(), o.getWidth(), o.getHeight()))) {
                return true;
            }
        }
        return false;
    }

    private void handleSkills() {
        if (input.isS1() && player.getCooldownS1() == 0 && player.getRemainingBullets() >= 5) {
            soundManager.playEffect("sfx_keyboard.wav");
            player.addBullets(-5);
            player.setS1RemainingShots(3);
            player.setCooldownS1(600); 
        }

        if (input.isS2() && player.getCooldownS2() == 0 && player.getRemainingBullets() >= 10) {
            soundManager.playEffect("sfx_skill_meteor.wav");    
            player.addBullets(-10);
            int holeSize = 160; 
            int targetX = (input.getMouseX() > 0) ? input.getMouseX() : player.getX() + 25;
            int targetY = (input.getMouseY() > 0) ? input.getMouseY() : player.getY() + 25;
            int tx = targetX - (holeSize / 2);
            int ty = targetY - (holeSize / 2);
            
            yetis.removeIf(y -> {
                double dist = Math.sqrt(Math.pow(y.getX() - targetX, 2) + Math.pow(y.getY() - targetY, 2));
                if (dist < 200) { player.registerKill(100); return true; }
                return false;
            });
            
            obstacles.add(new Obstacle(tx, ty, holeSize, holeSize, true, 300)); 
            player.setCooldownS2(900); 
        }

        if (input.isS3() && player.getCooldownS3() == 0 && player.getRemainingBullets() >= 3) {
            soundManager.playEffect("sfx_skill_ghost.wav");
            player.addBullets(-3);
            player.setGhostDuration(300); 
            player.setCooldownS3(600); 
        }
    }

    private void handleCombat() {
        boolean wantToShoot = input.isShooting() || (settings.isUseMouse() && input.isMouseClicked());
        
        if (wantToShoot && shootCooldown == 0 && (player.getRemainingBullets() > 0 || player.getS1RemainingShots() > 0)) {
            double targetDx, targetDy;
            if (settings.isUseMouse() && input.isMouseClicked()) {
                targetDx = input.getMouseX() - (player.getX() + 25);
                targetDy = input.getMouseY() - (player.getY() + 25);
            } else {
                targetDx = player.getLastDx(); 
                targetDy = player.getLastDy();
            }

            // PENYESUAIAN POSISI SPAWN BERDASARKAN UKURAN PELURU
            int startX, startY;
            boolean isGiant = player.getS1RemainingShots() > 0;
            
            if (isGiant) {
                // Giant (80x80) center ke Penguin (50x50). Offset = (50-80)/2 = -15
                startX = player.getX() - 15;
                startY = player.getY() - 15;
            } else {
                // Normal (15x15) center ke Penguin (50x50). Offset = (50-15)/2 = 17
                startX = player.getX() + 17;
                startY = player.getY() + 17;
            }

            Projectile p = new Projectile(startX, startY, targetDx, targetDy, Projectile.PLAYER_TYPE, 8);
            
            if (isGiant) {
                p.setPiercing(true);
                player.useS1Shot();
                soundManager.playEffect("sfx_skill_giant.wav");
            } else {
                player.useBullet();
                soundManager.playEffect("sfx_shoot.wav");
            }
            
            projectiles.add(p);
            shootCooldown = 15;
        } else if (wantToShoot && shootCooldown == 0 && player.getRemainingBullets() <= 0 && player.getS1RemainingShots() <= 0) {
            soundManager.playEffect("sfx_low_ammo.wav");
            shootCooldown = 15;
        }
        if (shootCooldown > 0) shootCooldown--;

        for (Yeti y : yetis) {
            if (rand.nextInt(300) < 2) {
                soundManager.playEffect("sfx_yeti_shoot.wav");
                projectiles.add(new Projectile(y.getX()+30, y.getY()+30, player.getX()-y.getX(), player.getY()-y.getY(), Projectile.YETI_TYPE, 6));
            }
        }

        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.update();
            
            if (!p.isPiercing()) {
                for (Obstacle o : obstacles) {
                    if (!o.isHole() && p.getBounds().intersects(new Rectangle(o.getX(), o.getY(), o.getWidth(), o.getHeight()))) {
                        o.takeDamage(); 
                        soundManager.playEffect("sfx_hit_obstacle.wav");
                        p.setActive(false); 
                        break;
                    }
                }
            }

            if (!p.isActive() || p.getX() < 0 || p.getX() > 800 || p.getY() < 0 || p.getY() > 600) {
                if (p.getOwner().equals(Projectile.YETI_TYPE)) player.addBullets(1);
                else if (p.getOwner().equals(Projectile.PLAYER_TYPE) && !p.isHit()) player.registerMiss();
                it.remove();
            }
        }
    }

    private void checkCollisions() {
        Rectangle pBounds = player.getBounds();
        for (Yeti y : yetis) {
            if (!player.isGhost() && y.getBounds().intersects(pBounds)) {
                player.die();
                return;
            }
            for (Projectile p : projectiles) {
                if (p.getOwner().equals(Projectile.PLAYER_TYPE) && p.getBounds().intersects(y.getBounds())) {
                    y.takeDamage(100);
                    p.setHit(true);
                    if (!p.isPiercing()) p.setActive(false);
                    if (!y.isAlive()){
                        soundManager.playEffect("sfx_yeti_die.wav");
                        player.registerKill(100);
                    }
                }
            }
        }
        for (Projectile p : projectiles) {
            if (p.getOwner().equals(Projectile.YETI_TYPE) && !player.isGhost() && p.getBounds().intersects(pBounds)) {
                player.die();
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
        view.requestFocusInWindow();
        gameLoop = new Timer(16, e -> update());
        gameLoop.start(); 
    }

    public void surrender() {
        player.die();
        update();
    }
}