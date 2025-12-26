package com.lastpenguin.model;

import java.awt.Rectangle;

public class Projectile {
    public static final String PLAYER_TYPE = "PLAYER";
    public static final String YETI_TYPE = "YETI";

    private double x, y;
    private double dx, dy;
    private String owner;
    private boolean active = true;
    private boolean hit = false;
    private int speed;
    private boolean piercing = false; // Properti Baru

    public Projectile(double x, double y, double targetDx, double targetDy, String owner, int speed) {
        this.x = x;
        this.y = y;
        this.owner = owner;
        this.speed = speed;

        // Normalisasi arah agar kecepatan peluru konsisten
        double distance = Math.sqrt(targetDx * targetDx + targetDy * targetDy);
        if (distance > 0) {
            this.dx = (targetDx / distance) * speed;
            this.dy = (targetDy / distance) * speed;
        }
    }

    public void update() {
        x += dx;
        y += dy;
    }

    public Rectangle getBounds() {
        int size = piercing ? 40 : 12; // Ukuran hitbox lebih besar jika piercing
        return new Rectangle((int) x, (int) y, size, size);
    }

    // GETTER & SETTER BARU
    public boolean isPiercing() { return piercing; }
    public void setPiercing(boolean piercing) { this.piercing = piercing; }

    public int getX() { return (int) x; }
    public int getY() { return (int) y; }
    public String getOwner() { return owner; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isHit() { return hit; }
    public void setHit(boolean hit) { this.hit = hit; }
}