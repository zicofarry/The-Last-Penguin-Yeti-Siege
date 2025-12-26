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
                soundManager.playEffect("sfx_game_over.wav");
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
        
        int spawnRate = settings.getDifficulty().equals(GameSettings.HARD) ? 80 : 
                        settings.getDifficulty().equals(GameSettings.MEDIUM) ? 120 : 200;
        spawnTimer++;
        if (spawnTimer > spawnRate) {
            yetis.add(new Yeti(rand.nextInt(700), 580, settings.getDifficulty().equals(GameSettings.HARD) ? 3 : 1)); 
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

            // Hitung arah dasar
            int moveDx = 0, moveDy = 0;
            if (oldX < targetX) moveDx = y.getSpeed();
            else if (oldX > targetX) moveDx = -y.getSpeed();
            
            if (oldY < targetY) moveDy = y.getSpeed();
            else if (oldY > targetY) moveDy = -y.getSpeed();

            boolean movedX = false;
            boolean movedY = false;

            // 1. Coba gerak horizontal (X)
            if (moveDx != 0) {
                y.setPosition(oldX + moveDx, oldY);
                if (!isYetiColliding(y)) movedX = true;
                else y.setPosition(oldX, oldY); // Batal jika tabrakan
            }

            // 2. Coba gerak vertikal (Y)
            if (moveDy != 0) {
                y.setPosition(y.getX(), oldY + moveDy); // Gunakan X terbaru (hasil langkah 1)
                if (!isYetiColliding(y)) movedY = true;
                else y.setPosition(y.getX(), oldY); // Batal jika tabrakan
            }

            // 3. LOGIKA BYPASS (Mencegah Stuck di sumbu yang sama)
            // Jika Yeti tidak bisa bergerak maju padahal belum sampai ke target
            if (!movedX && !movedY) {
                if (moveDx == 0 && moveDy != 0) { 
                    // Sumbu X sudah sejajar tapi Y terhalang. Geser X secara paksa.
                    y.setPosition(oldX + y.getSpeed(), oldY);
                    if (isYetiColliding(y)) y.setPosition(oldX - y.getSpeed(), oldY);
                } 
                else if (moveDy == 0 && moveDx != 0) {
                    // Sumbu Y sudah sejajar tapi X terhalang. Geser Y secara paksa.
                    y.setPosition(oldX, oldY + y.getSpeed());
                    if (isYetiColliding(y)) y.setPosition(oldX, oldY - y.getSpeed());
                }
            }
            
            // Update arah visual/animasi berdasarkan target
            y.updateAnimation(targetX, targetY);
        }
    }

    // Helper untuk cek tabrakan Yeti
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
            
            // Tentukan ukuran lubang yang lebih besar
            int holeSize = 160; 
            
            // Tentukan koordinat target (tengah kursor atau tengah penguin)
            int targetX = (input.getMouseX() > 0) ? input.getMouseX() : player.getX() + 25;
            int targetY = (input.getMouseY() > 0) ? input.getMouseY() : player.getY() + 25;

            // Hitung posisi sudut kiri atas agar gambar tepat di tengah target
            int tx = targetX - (holeSize / 2);
            int ty = targetY - (holeSize / 2);
            
            // Logika kill tetap luas (radius 200)
            yetis.removeIf(y -> {
                double dist = Math.sqrt(Math.pow(y.getX() - targetX, 2) + Math.pow(y.getY() - targetY, 2));
                if (dist < 200) { player.registerKill(100); return true; }
                return false;
            });
            
            // Tambahkan lubang dengan ukuran baru (160x160)
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
        if (wantToShoot){
            if(player.getRemainingBullets() > 0 && shootCooldown == 0) {
                if (player.getS1RemainingShots() > 0) {
                    soundManager.playEffect("sfx_skill_giant.wav");
                } else {
                    soundManager.playEffect("sfx_shoot.wav");
                }
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
                    p.setPiercing(true);
                    player.useS1Shot();
                }
                projectiles.add(p);
                player.useBullet();
                shootCooldown = 15;
            } else if (player.getRemainingBullets() <= 0 && input.isMouseClicked()) {
                soundManager.playEffect("sfx_low_ammo.wav"); // Klik saat peluru habis
            }
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
                        p.setActive(false); break;
                    }
                }
            }

            if (!p.isActive() || p.getX() < 0 || p.getX() > 800 || p.getY() < 0 || p.getY() > 600) {
                if (p.getOwner().equals(Projectile.YETI_TYPE)) {
                    player.addBullets(1);
                } else if (p.getOwner().equals(Projectile.PLAYER_TYPE) && !p.isHit()) {
                    player.registerMiss();
                }
                it.remove();
            }
        }
    }

    private void checkCollisions() {
        Rectangle pBounds = player.getBounds();

        for (Yeti y : yetis) {
            // KONTAK FISIK: Penguin mati jika ditabrak Yeti
            if (!player.isGhost() && y.getBounds().intersects(pBounds)) {
                player.die();
                return;
            }
            
            // Peluru Player mengenai Yeti
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

        // Peluru Yeti mengenai Penguin
        for (Projectile p : projectiles) {
            if (p.getOwner().equals(Projectile.YETI_TYPE)) {
                if (!player.isGhost() && p.getBounds().intersects(pBounds)) {
                    player.die();
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
        view.requestFocusInWindow();
        gameLoop = new Timer(16, e -> update());
        gameLoop.start(); 
    }

    public void surrender() {
        player.die();
        update();
    }
}