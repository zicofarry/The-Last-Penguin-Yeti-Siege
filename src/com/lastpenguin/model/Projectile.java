package com.lastpenguin.model;

import java.awt.Rectangle;

/**
 * Projectile that moves in a specific direction (vx, vy).
 */
public class Projectile {
    public static final String PLAYER_TYPE = "PLAYER";
    public static final String YETI_TYPE = "YETI";

    private int x, y, vx, vy;
    private String owner;
    private boolean active = true;

    public Projectile(int x, int y, int vx, int vy, String owner) {
        this.x = x;
        this.y = y;
        this.vx = vx * 8; 
        this.vy = vy * 8;
        this.owner = owner;
    }

    public void update() {
        this.x += vx;
        this.y += vy;
        if (y < -50 || y > 650 || x < -50 || x > 850) active = false;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 12, 12);
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getOwner() { return owner; }
    public int getX() { return x; }
    public int getY() { return y; }
}