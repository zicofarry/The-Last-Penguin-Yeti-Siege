/**
 * Copyright (c) 2025 Muhammad 'Azmi Salam. All Rights Reserved.
 * Email: mhmmdzmslm36@gmail.com
 * GitHub: https://github.com/zicofarry
 */
package com.lastpenguin.model;

import java.awt.Rectangle;

/**
 * Represents the main Penguin character.
 * Handles movement boundaries and ammunition scavenging logic.
 * @author Muhammad 'Azmi Salam
 * @version 1.5
 */
public class Player {
    private String username;
    private int x, y, score, remainingBullets, yetiKilled, missedShots;
    private int lastDx = 0, lastDy = -1; // Default facing Up
    private boolean alive = true;

    public Player(String username) {
        this.username = username;
        this.x = 375; // Initial center position
        this.y = 200;
        this.remainingBullets = 0; 
        this.score = 0;
        this.yetiKilled = 0;
        this.missedShots = 0;
    }

    /**
     * Moves the player while keeping them within the arena bounds.
     */
    public void move(int dx, int dy) {
        if (!alive) return;
        this.x += dx * 5;
        this.y += dy * 5;

        // Store facing direction if moving
        if (dx != 0 || dy != 0) {
            this.lastDx = dx;
            this.lastDy = dy;
        }

        // Boundary checks (800x600 arena)
        if (x < 0) x = 0;
        if (x > 750) x = 750;
        if (y < 0) y = 0;
        if (y > 550) y = 550;
    }

    /**
     * Gets the collision boundaries of the player.
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, 40, 40);
    }

    public void die() { this.alive = false; }
    public boolean isAlive() { return alive; }
    public int getLastDx() { return lastDx; }
    public int getLastDy() { return lastDy; }
    
    // Getters and helper methods
    public int getX() { return x; }
    public int getY() { return y; }
    public int getScore() { return score; }
    public int getRemainingBullets() { return remainingBullets; }
    public int getYetiKilled() { return yetiKilled; }
    public int getMissedShots() { return missedShots; }
    public String getUsername() { return username; }
    public int getHealth() { return alive ? 100 : 0; } // HP compatibility for HUD

    public void addBullets(int count) { this.remainingBullets += count; }
    public void useBullet() { if (remainingBullets > 0) remainingBullets--; }
    public void registerMiss() { this.missedShots++; }
    public void registerKill(int points) { 
        this.score += points; 
        this.yetiKilled++;
    }
}