package com.lastpenguin.model;

/**
 * Represents a transient visual particle used for environmental effects or feedback.
 * This class manages small-scale visual elements such as snowflakes or impact sparks,
 * handling their trajectory based on random velocity and their automated lifecycle.
 */
public class Particle {
    private double x, y;
    private double vx, vy; 
    private int lifeSpan; 
    private boolean active;

    /**
     * Constructs a new particle at the specified coordinates with a randomized velocity.
     * @param x The initial horizontal spawn position.
     * @param y The initial vertical spawn position.
     * @param life The initial lifespan of the particle measured in game frames.
     */
    public Particle(int x, int y, int life) {
        this.x = x;
        this.y = y;
        this.lifeSpan = life;
        this.active = true;
        
        // Initializes randomized movement vectors for natural visual variety
        this.vx = Math.random() * 4 - 2;
        this.vy = Math.random() * 4 - 2;
    }

    /**
     * Updates the particle's state on each game tick.
     * Translates the particle based on its velocity and decrements its remaining lifespan.
     * The particle becomes inactive once its lifespan reaches zero.
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

    // --- Getter Methods for Rendering Synchronization ---

    public int getX() { return (int) x; }
    public int getY() { return (int) y; }
    public boolean isActive() { return active; }
}