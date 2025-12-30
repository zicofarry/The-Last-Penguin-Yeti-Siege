package com.lastpenguin.model;

/**
 * Represents static or destructible environmental objects within the game arena.
 * Obstacles serve as barriers that impede entity movement and intercept projectiles.
 * This implementation supports a durability system (HP) for destructible objects 
 * and duration-based logic for temporary environmental hazards.
 */
public class Obstacle {
    private int x, y;
    private int width, height;
    private boolean isHole = false;
    private int duration = -1;
    
    // Durability features for destructible obstacles
    private int hp = -1; 
    private boolean destructible = false;

    /**
     * Standard constructor for permanent, indestructible obstacles.
     * @param x The horizontal position of the obstacle.
     * @param y The vertical position of the obstacle.
     * @param width The width of the obstacle's collision area.
     * @param height The height of the obstacle's collision area.
     */
    public Obstacle(int x, int y, int width, int height) {
        this.x = x; 
        this.y = y; 
        this.width = width; 
        this.height = height;
    }

    /**
     * Constructor for destructible obstacles with health points (e.g., Ice Rocks or Spikes).
     * @param initialHp The initial structural integrity points.
     */
    public Obstacle(int x, int y, int width, int height, int initialHp) {
        this(x, y, width, height);
        this.hp = initialHp;
        this.destructible = true;
    }

    /**
     * Constructor for hazardous terrain zones (e.g., holes created by Meteor strikes).
     * @param isHole Flag to identify the obstacle as a hazard zone.
     * @param duration The lifespan of the hazard measured in game frames.
     */
    public Obstacle(int x, int y, int width, int height, boolean isHole, int duration) {
        this(x, y, width, height);
        this.isHole = isHole;
        this.duration = duration;
    }

    /**
     * Decrements the structural integrity of the obstacle when struck by a projectile.
     * Only affects obstacles marked as destructible.
     */
    public void takeDamage() {
        if (destructible && hp > 0) {
            hp--;
        }
    }

    /**
     * Updates the obstacle's state on each game tick.
     * Primarily manages the countdown for temporary hazard durations.
     */
    public void update() { 
        if (duration > 0) duration--; 
    }

    /**
     * Determines if a temporary hazard has reached the end of its lifespan.
     * @return True if the object is a hole and its duration has elapsed.
     */
    public boolean isExpired() { 
        return isHole && duration == 0; 
    }

    /**
     * Determines if a destructible obstacle has been fully broken.
     * @return True if the obstacle is destructible and its health points are depleted.
     */
    public boolean isDestroyed() {
        return destructible && hp <= 0;
    }

    // --- Getter Methods for Rendering and Collision Logic ---

    public boolean isHole() { return isHole; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getHp() { return hp; }
    public boolean isDestructible() { return destructible; }
}