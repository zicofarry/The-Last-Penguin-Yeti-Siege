package com.lastpenguin.model;

import java.awt.Rectangle;

/**
 * Represents the player entity within the game.
 * Manages movement constraints, ammunition tracking, scoring statistics, 
 * and the state of special abilities and buffs.
 */
public class Player {
    private String username;
    private int x, y, score, remainingBullets, yetiKilled, missedShots;
    private int lastDx = 0, lastDy = -1; 
    private boolean alive = true;
    
    private int cooldownS1 = 0, cooldownS2 = 0, cooldownS3 = 0;
    private int s1RemainingShots = 0; 
    private int ghostDuration = 0;    

    public Player(String username, int startingBullets) {
        this.username = username;
        this.x = 375; 
        this.y = 200;
        this.remainingBullets = startingBullets; 
        this.score = 0;
        this.yetiKilled = 0;
        this.missedShots = 0;
    }

    /**
     * Updates the player's position based on input delta and enforces arena boundaries.
     */
    public void move(int dx, int dy) {
        if (!alive) return;
        this.x += dx * 5;
        this.y += dy * 5;

        if (dx != 0 || dy != 0) {
            this.lastDx = dx;
            this.lastDy = dy;
        }

        if (x < 0) x = 0;
        if (x > 750) x = 750;
        if (y < 0) y = 0;
        if (y > 550) y = 550;
    }

    /**
     * Returns the bounding box for collision detection.
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, 40, 40);
    }

    public void die() { this.alive = false; }
    public boolean isAlive() { return alive; }
    
    /**
     * Determines if the Giant Snowball buff is currently active.
     */
    public boolean isGiantBuffActive() {
        return s1RemainingShots > 0;
    }

    /**
     * Decrements skill cooldowns and active buff durations on each game tick.
     */
    public void updateTimers() {
        if (cooldownS1 > 0) cooldownS1--;
        if (cooldownS2 > 0) cooldownS2--;
        if (cooldownS3 > 0) cooldownS3--;
        
        if (ghostDuration > 0) ghostDuration--;
    }

    public void addBullets(int count) { this.remainingBullets += count; }
    
    public void useBullet() { 
        if (remainingBullets > 0) remainingBullets--; 
    }
    
    public void registerMiss() { this.missedShots++; }
    
    public void registerKill(int points) { 
        this.score += points; 
        this.yetiKilled++;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getScore() { return score; }
    public int getRemainingBullets() { return remainingBullets; }
    public int getYetiKilled() { return yetiKilled; }
    public int getMissedShots() { return missedShots; }
    public String getUsername() { return username; }
    public int getHealth() { return alive ? 100 : 0; }
    public int getLastDx() { return lastDx; }
    public int getLastDy() { return lastDy; }

    public int getCooldownS1() { return cooldownS1; }
    public void setCooldownS1(int v) { this.cooldownS1 = v; }
    public int getS1RemainingShots() { return s1RemainingShots; }
    public void setS1RemainingShots(int v) { this.s1RemainingShots = v; }
    
    /**
     * Consumes one charge of the Giant Snowball skill.
     */
    public void useS1Shot() { 
        if (s1RemainingShots > 0) {
            s1RemainingShots--; 
        }
    }

    public int getCooldownS2() { return cooldownS2; }
    public void setCooldownS2(int v) { this.cooldownS2 = v; }

    public int getCooldownS3() { return cooldownS3; }
    public void setCooldownS3(int v) { this.cooldownS3 = v; }
    public boolean isGhost() { return ghostDuration > 0; }
    public void setGhostDuration(int v) { this.ghostDuration = v; }
}