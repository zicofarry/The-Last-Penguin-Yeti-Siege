package com.lastpenguin.model;

/**
 * Represents a meteor projectile used in special skills. 
 * Manages falling physics, target coordinates, and landing state.
 */
public class Meteor {
    private int x, y, targetX, targetY;
    private int speed = 20; 
    private boolean landed = false;

    /**
     * Initializes a meteor starting from the top of the screen toward a target.
     */
    public Meteor(int tx, int ty) {
        this.targetX = tx;
        this.targetY = ty;
        this.x = tx - 40; 
        this.y = -100;    
    }

    /**
     * Updates the vertical position of the meteor until it reaches the target coordinates.
     */
    public void update() {
        if (y < targetY) {
            y += speed;
            if (y >= targetY) {
                y = targetY;
                landed = true;
            }
        }
    }

    public boolean isLanded() { return landed; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getTargetX() { return targetX; }
    public int getTargetY() { return targetY; }
}