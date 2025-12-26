/**
 * Copyright (c) 2025 Muhammad 'Azmi Salam. All Rights Reserved.
 * Email: mhmmdzmslm36@gmail.com
 * GitHub: https://github.com/zicofarry
 */
package com.lastpenguin.model;

/**
 * Represents static environmental objects like ice rocks or snow walls.
 * Obstacles block the movement of entities and destroy projectiles.
 * * @author Muhammad 'Azmi Salam
 * @version 1.0
 * @since December 2025
 */
public class Obstacle {
    private int x, y;
    private int width, height;
    private boolean isHole = false;
    private int duration = -1;

    /**
     * Constructs a new static obstacle.
     * @param x Initial X position.
     * @param y Initial Y position.
     * @param width Width of the obstacle.
     * @param height Height of the obstacle.
     */
    public Obstacle(int x, int y, int width, int height) {
        this.x = x; this.y = y; this.width = width; this.height = height;
    }

    public Obstacle(int x, int y, int width, int height, boolean isHole, int duration) {
        this(x, y, width, height);
        this.isHole = isHole;
        this.duration = duration;
    }

    public void update() { if (duration > 0) duration--; }
    public boolean isExpired() { return isHole && duration == 0; }
    public boolean isHole() { return isHole; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
