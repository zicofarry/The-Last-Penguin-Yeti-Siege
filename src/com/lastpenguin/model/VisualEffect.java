package com.lastpenguin.model;

/**
 * Represents a transient visual effect within the game environment.
 * This class manages the lifecycle and transparency of temporary graphical 
 * elements, allowing for time-based animations and automatic resource cleanup.
 */
public class VisualEffect {
    private int x, y;
    private int currentFrame;
    private int maxFrames;
    private boolean active;
    private String type; 

    /**
     * Initializes a new visual effect at a specific coordinate.
     * @param x The horizontal position of the effect.
     * @param y The vertical position of the effect.
     * @param duration The total lifespan of the effect in game frames.
     * @param type The categorical identifier used to determine the rendered asset.
     */
    public VisualEffect(int x, int y, int duration, String type) {
        this.x = x;
        this.y = y;
        this.maxFrames = duration;
        this.currentFrame = 0;
        this.active = true;
        this.type = type;
    }

    /**
     * Increments the internal frame counter on each game tick.
     * Automatically deactivates the effect once its maximum frame count is reached.
     */
    public void update() {
        if (!active) return;
        currentFrame++;
        if (currentFrame >= maxFrames) active = false;
    }

    // --- Getter Methods for Rendering and Logic Synchronization ---

    public int getX() { return x; }
    public int getY() { return y; }
    public String getType() { return type; }
    public boolean isActive() { return active; }
    
    /**
     * Calculates the current alpha/transparency level based on remaining duration.
     * @return A float value between 1.0 (fully visible) and 0.0 (fully transparent).
     */
    public float getOpacity() { return 1.0f - ((float)currentFrame / maxFrames); }
}