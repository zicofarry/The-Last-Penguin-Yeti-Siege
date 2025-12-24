/**
 * Copyright (c) 2025 Muhammad 'Azmi Salam. All Rights Reserved.
 * Email: mhmmdzmslm36@gmail.com
 * GitHub: https://github.com/zicofarry
 */
package com.lastpenguin.model;

/**
 * Represents larger graphical effects such as explosions or fire blasts.
 * These effects usually cycle through animation frames.
 * * @author Muhammad 'Azmi Salam
 * @version 1.0
 * @since December 2025
 */
public class VisualEffect {
    private int x, y;
    private int currentFrame;
    private int maxFrames;
    private boolean active;

    /**
     * Constructs a visual effect at a location.
     * @param x Spawn X position.
     * @param y Spawn Y position.
     * @param duration Number of frames the animation lasts.
     */
    public VisualEffect(int x, int y, int duration) {
        this.x = x;
        this.y = y;
        this.maxFrames = duration;
        this.currentFrame = 0;
        this.active = true;
    }

    /**
     * Advances the animation frame.
     */
    public void update() {
        if (!active) return;

        currentFrame++;
        if (currentFrame >= maxFrames) {
            active = false;
        }
    }

    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getCurrentFrame() { return currentFrame; }
    public boolean isActive() { return active; }
}
