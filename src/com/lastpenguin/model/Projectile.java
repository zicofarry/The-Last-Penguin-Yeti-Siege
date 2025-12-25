package com.lastpenguin.model;

import java.awt.Rectangle;

public class Projectile {
    public static final String PLAYER_TYPE = "PLAYER";
    public static final String YETI_TYPE = "YETI";

    private double x, y, vx, vy; // Ubah ke double untuk presisi sudut
    private String owner;
    private boolean active = true;
    private boolean hit = false;

    public Projectile(int x, int y, double vx, double vy, String owner, int speed) {
        this.x = x;
        this.y = y;
        // Normalisasi vektor dan kalikan dengan speed
        double magnitude = Math.sqrt(vx * vx + vy * vy);
        if (magnitude != 0) {
            this.vx = (vx / magnitude) * speed;
            this.vy = (vy / magnitude) * speed;
        }
        this.owner = owner;
    }

    public void update() {
        this.x += vx;
        this.y += vy;
        if (y < -50 || y > 650 || x < -50 || x > 850) active = false;
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, 12, 12);
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isHit() { return hit; }
    public void setHit(boolean hit) { this.hit = hit; }
    public String getOwner() { return owner; }
    public int getX() { return (int)x; }
    public int getY() { return (int)y; }
}