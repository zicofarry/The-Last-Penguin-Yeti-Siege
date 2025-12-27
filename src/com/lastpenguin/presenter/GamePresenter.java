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
    private List<Meteor> activeMeteors = new ArrayList<>(); // List untuk meteor yang sedang jatuh
    private boolean isTargetingMeteor = false; // Mode membidik meteor
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

    private void spawnOneRandomObstacle() {
        Rectangle playerSafeZone = new Rectangle(player.getX() - 50, player.getY() - 50, 150, 150);
        int ox, oy, initialHp;
        Rectangle obsRect;
        do {
            ox = rand.nextInt(600) + 100;
            oy = rand.nextInt(350) + 50;
            obsRect = new Rectangle(ox, oy, 80, 80);
        } while (obsRect.intersects(playerSafeZone));
        
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
        
        // 4. UPDATE ANIMASI METEOR JATUH
        Iterator<Meteor> metIt = activeMeteors.iterator();
        while (metIt.hasNext()) {
            Meteor m = metIt.next();
            m.update();
            if (m.isLanded()) {
                triggerMeteorImpact(m); // Meledak saat menyentuh target
                metIt.remove();
            }
        }
        
        // 5. LOGIKA GAMEPLAY UTAMA
        player.updateTimers();
        handleSkills();
        handleMovement();
        handleCombat();
        checkCollisions();
        
        // --- UPDATE OBSTACLES ---
        Iterator<Obstacle> obsIt = obstacles.iterator();
        while (obsIt.hasNext()) {
            Obstacle o = obsIt.next();
            o.update();
            if (o.isDestroyed()) {
                soundManager.playEffect("sfx_ice_break.wav");
                obsIt.remove();
            } else if (o.isExpired()) {
                obsIt.remove();
            }
        }

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

        // PERGERAKAN YETI
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
            // FIX: Yeti sekarang terhalang oleh semua rintangan termasuk Lubang Meteor
            if (yBounds.intersects(new Rectangle(o.getX(), o.getY(), o.getWidth(), o.getHeight()))) {
                return true;
            }
        }
        return false;
    }

    private void handleSkills() {
        // SKILL 1: GIANT SNOWBALL
        if (input.isS1() && player.getCooldownS1() == 0 && player.getRemainingBullets() >= 5) {
            soundManager.playEffect("sfx_keyboard.wav");
            player.addBullets(-5);
            player.setS1RemainingShots(3);
            player.setCooldownS1(600); 
        }

        // SKILL 2: METEOR STRIKE (LOGIKA BARU)
        if (input.isS2() && player.getCooldownS2() == 0 && player.getRemainingBullets() >= 10) {
            if (settings.isUseMouse()) {
                isTargetingMeteor = !isTargetingMeteor; // Aktifkan mode bidik
            } else {
                // Jika mouse mati, langsung spawn di posisi penguin
                spawnMeteor(player.getX() + 25, player.getY() + 25);
            }
            // Reset status input agar tidak toggle berkali-kali dalam satu pencetan
            // (Tergantung implementasi InputHandler, jika perlu manual reset)
        }

        // SKILL 3: GHOST MODE
        if (input.isS3() && player.getCooldownS3() == 0 && player.getRemainingBullets() >= 3) {
            soundManager.playEffect("sfx_skill_ghost.wav");
            player.addBullets(-3);
            player.setGhostDuration(300); 
            player.setCooldownS3(600); 
        }
    }

    private void handleCombat() {
        // LOGIKA KLIK MOUSE UNTUK METEOR
        if (isTargetingMeteor && input.isMouseClicked()) {
            spawnMeteor(input.getMouseX(), input.getMouseY());
            isTargetingMeteor = false;
            return; // Jangan tembak peluru biasa jika sedang klik meteor
        }

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

            int startX, startY;
            boolean isGiant = player.getS1RemainingShots() > 0;
            
            if (isGiant) {
                startX = player.getX() - 15;
                startY = player.getY() - 15;
            } else {
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

        // Yeti Shooting
        for (Yeti y : yetis) {
            if (rand.nextInt(300) < 2) {
                soundManager.playEffect("sfx_yeti_shoot.wav");
                projectiles.add(new Projectile(y.getX()+30, y.getY()+30, player.getX()-y.getX(), player.getY()-y.getY(), Projectile.YETI_TYPE, 6));
            }
        }

        // Projectile Update
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

    private void spawnMeteor(int tx, int ty) {
        player.addBullets(-10);
        player.setCooldownS2(900);
        activeMeteors.add(new Meteor(tx, ty)); // Meteor mulai jatuh dari langit
    }

    private void triggerMeteorImpact(Meteor m) {
        soundManager.playEffect("sfx_skill_meteor.wav"); // Suara diputar saat meteor menyentuh tanah
        int targetX = m.getTargetX();
        int targetY = m.getTargetY();
        int holeW = 160; 
        int holeH = (int)(holeW * 321.0 / 500.0);
        int tx = targetX - (holeW / 2);
        int ty = targetY - (holeH / 2);
        
        // Hancurkan Yeti di sekitar titik jatuh
        yetis.removeIf(y -> {
            double dist = Math.sqrt(Math.pow(y.getX() + 30 - targetX, 2) + Math.pow(y.getY() + 30 - targetY, 2));
            if (dist < 150) { 
                player.registerKill(100); 
                soundManager.playEffect("sfx_yeti_die.wav");
                return true; 
            }
            return false;
        });
        
        // Munculkan rintangan lubang
        obstacles.add(new Obstacle(tx, ty, holeW, holeH, true, 300)); 
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

    // Getters
    public List<Obstacle> getObstacles() { return obstacles; }
    public List<Yeti> getYetis() { return yetis; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public List<Meteor> getActiveMeteors() { return activeMeteors; }
    public boolean isTargetingMeteor() { return isTargetingMeteor; }
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