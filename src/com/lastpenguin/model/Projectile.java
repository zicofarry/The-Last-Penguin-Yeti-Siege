package com.lastpenguin.model;

/**
 * Represents projectiles fired by either the Player or Yeti.
 */
public class Projectile {
    public static final String PLAYER_TYPE = "PLAYER";
    public static final String YETI_TYPE = "YETI";

    private int x, y, speed;
    private String owner;
    private boolean active = true;

    public Projectile(int x, int y, String owner) {
        this.x = x;
        this.y = y;
        this.owner = owner;
        // Player shoots up, Yeti shoots from below moving up
        this.speed = owner.equals(PLAYER_TYPE) ? -10 : -6; 
    }

    public void update() {
        this.y += speed;
        if (y < -50 || y > 650) active = false;
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getOwner() { return owner; }
    public int getX() { return x; }
    public int getY() { return y; }
}