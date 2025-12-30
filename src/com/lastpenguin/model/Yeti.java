package com.lastpenguin.model;

import java.awt.Rectangle;

/**
 * Represents the enemy Yeti entity.
 * Handles AI pathfinding toward the player, directional animation state,
 * and health management.
 */
public class Yeti {
    public static final int FRONT = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int BACK = 3;

    private int x, y, health, speed;
    private boolean alive = true;
    
    private int direction = FRONT;
    private int animationStep = 1; 
    private int animationCounter = 0;

    public Yeti(int x, int y, int difficultyValue) {
        this.x = x;
        this.y = y;
        this.health = 50;
        this.speed = (difficultyValue < 1) ? 1 : difficultyValue;
    }

    /**
     * Moves the Yeti toward the player's coordinates and updates the facing direction.
     */
    public void trackPlayer(int playerX, int playerY) {
        if (!alive) return;

        int dx = 0, dy = 0;

        if (this.x < playerX) { dx = speed; direction = RIGHT; }
        else if (this.x > playerX) { dx = -speed; direction = LEFT; }

        if (this.y < playerY) { dy = speed; direction = FRONT; }
        else if (this.y > playerY) { dy = -speed; direction = BACK; }
        
        if (Math.abs(playerX - x) > Math.abs(playerY - y)) {
            direction = (playerX > x) ? RIGHT : LEFT;
        } else {
            direction = (playerY > y) ? FRONT : BACK;
        }

        this.x += dx;
        this.y += dy;

        updateAnimationTick();
    }

    /**
     * Updates the directional orientation based on the target position.
     */
    public void updateAnimation(int targetX, int targetY) {
        if (Math.abs(targetX - x) > Math.abs(targetY - y)) {
            direction = (targetX > x) ? RIGHT : LEFT;
        } else {
            direction = (targetY > y) ? FRONT : BACK;
        }

        updateAnimationTick();
    }

    /**
     * Logic for alternating walking animation frames.
     */
    private void updateAnimationTick() {
        animationCounter++;
        if (animationCounter > 12) {
            animationStep++;
            if (animationStep > 2) animationStep = 0;
            animationCounter = 0;
        }
    }

    public void moveBack(int dx, int dy) {
        this.x -= dx;
        this.y -= dy;
    }

    /**
     * Calculates the sprite index based on direction and current animation step.
     */
    public int getSpriteIndex() {
        return (direction * 3) + animationStep;
    }

    public Rectangle getBounds() { return new Rectangle(x, y, 60, 60); }
    
    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health <= 0) alive = false;
    }

    public boolean isAlive() { return alive; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getSpeed() { return speed; }
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}