/**
 * Copyright (c) 2025 Muhammad 'Azmi Salam. All Rights Reserved.
 * Email: mhmmdzmslm36@gmail.com
 * GitHub: https://github.com/zicofarry
 */
package com.lastpenguin.model;

/**
 * Represents the Yeti enemy in the game.
 * Handles enemy AI movement, health points, and status.
 * * @author Muhammad 'Azmi Salam
 * @version 1.0
 * @since December 2025
 */
public class Yeti {
    private int health;
    private int x, y;
    private int speed;
    private boolean isAlive;

    /**
     * Constructs a Yeti at a specific location.
     * @param x Initial X coordinate.
     * @param y Initial Y coordinate.
     * @param difficulty Adjusted speed based on game difficulty.
     */
    public Yeti(int x, int y, int difficulty) {
        this.x = x;
        this.y = y;
        this.health = 50; // Base health
        this.speed = 1 + difficulty; // Faster at higher difficulties
        this.isAlive = true;
    }

    /**
     * Logic for the Yeti to chase the player.
     * @param playerX Target X coordinate (Player's X).
     * @param playerY Target Y coordinate (Player's Y).
     */
    public void trackPlayer(int playerX, int playerY) {
        if (!isAlive) return;

        if (this.x < playerX) this.x += speed;
        else if (this.x > playerX) this.x -= speed;

        if (this.y < playerY) this.y += speed;
        else if (this.y > playerY) this.y -= speed;
    }

    /**
     * Reduces Yeti health when hit by a projectile.
     * @param damage Amount of health to subtract.
     */
    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health <= 0) {
            this.isAlive = false;
        }
    }

    // Standard Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isAlive() { return isAlive; }
    public int getHealth() { return health; }
}
