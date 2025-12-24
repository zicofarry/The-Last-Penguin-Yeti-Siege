/**
 * Copyright (c) 2025 Muhammad 'Azmi Salam. All Rights Reserved.
 * Email: mhmmdzmslm36@gmail.com
 * GitHub: https://github.com/zicofarry
 */
package com.lastpenguin.model;

/**
 * Represents the main Penguin character.
 * Handles movement boundaries and ammunition scavenging logic.
 * @author Muhammad 'Azmi Salam
 * @version 1.4
 */
public class Player {
    private String username;
    private int x, y, score, health, remainingBullets, yetiKilled, missedShots;

    public Player(String username) {
        this.username = username;
        this.x = 375;
        this.y = 200;
        this.health = 100;
        this.remainingBullets = 0; 
        this.score = 0;
        this.yetiKilled = 0;
        this.missedShots = 0;
    }

    /**
     * Moves the player while keeping them within the arena bounds.
     */
    public void move(int dx, int dy) {
        this.x += dx * 5;
        this.y += dy * 5;

        // Boundary checks
        if (x < 0) x = 0;
        if (x > 750) x = 750;
        if (y < 0) y = 0;
        if (y > 550) y = 550;
    }

    public void addBullets(int count) { this.remainingBullets += count; }
    public void useBullet() { if (remainingBullets > 0) remainingBullets--; }
    public void registerMiss() { this.missedShots++; }
    public void registerKill(int points) { 
        this.score += points; 
        this.yetiKilled++;
    }

    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getScore() { return score; }
    public int getHealth() { return health; }
    public int getRemainingBullets() { return remainingBullets; }
    public int getYetiKilled() { return yetiKilled; }
    public int getMissedShots() { return missedShots; }
    public String getUsername() { return username; }
}