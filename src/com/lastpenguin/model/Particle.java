/**
 * Copyright (c) 2025 Muhammad 'Azmi Salam. All Rights Reserved.
 * Email: mhmmdzmslm36@gmail.com
 * GitHub: https://github.com/zicofarry
 */
package com.lastpenguin.model;

/**
 * Represents a small, short-lived visual element for juice/feedback.
 * Used for snow flakes or hit sparks.
 * * @author Muhammad 'Azmi Salam
 * @version 1.0
 * @since December 2025
 */
public class Particle {
    private double x, y;
    private double vx, vy; // Velocity X and Y
    private int lifeSpan; // Remaining frames before disappearing
    private boolean active;

    /**
     * Constructs a new particle with random velocity.
     * @param x Spawn X position.
     * @param y Spawn Y position.
     * @param life Initial life span in frames.
     */
    public Particle(int x, int y, int life) {
        this.x = x;
        this.y = y;
        this.lifeSpan = life;
        this.active = true;
        
        // Give some random movement
        this.vx = Math.random() * 4 - 2;
        this.vy = Math.random() * 4 - 2;
    }

    /**
     * Updates the particle state.
     */
    public void update() {
        if (!active) return;

        x += vx;
        y += vy;
        lifeSpan--;

        if (lifeSpan <= 0) {
            active = false;
        }
    }

    // Getters
    public int getX() { return (int) x; }
    public int getY() { return (int) y; }
    public boolean isActive() { return active; }
}
