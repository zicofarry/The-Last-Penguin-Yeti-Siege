/**
 * Copyright (c) 2025 Muhammad 'Azmi Salam. All Rights Reserved.
 */
package com.lastpenguin.model;

import java.awt.Rectangle;

/**
 * Represents the Yeti enemy.
 */
public class Yeti {
    private int x, y, health, speed;
    private boolean alive = true;

    public Yeti(int x, int y, int difficultyValue) {
        this.x = x;
        this.y = y;
        this.health = 50;
        // Adjust speed based on difficulty: Easy = 1, Medium = 2, Hard = 3
        this.speed = difficultyValue;
    }

    public void trackPlayer(int playerX, int playerY) {
        if (!alive) return;
        // Basic movement logic; actual collision checked in Presenter
        if (this.x < playerX) this.x += speed;
        else if (this.x > playerX) this.x -= speed;

        if (this.y < playerY) this.y += speed;
        else if (this.y > playerY) this.y -= speed;
    }

    /**
     * Reverts movement if a collision is detected.
     */
    public void moveBack(int dx, int dy) {
        this.x -= dx;
        this.y -= dy;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 60, 60);
    }

    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health <= 0) alive = false;
    }

    public boolean isAlive() { return alive; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getSpeed() { return speed; }
}