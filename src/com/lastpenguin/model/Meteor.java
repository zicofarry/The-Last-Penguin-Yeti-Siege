package com.lastpenguin.model;

public class Meteor {
    private int x, y, targetX, targetY;
    private int speed = 20; // Kecepatan jatuh
    private boolean landed = false;

    public Meteor(int tx, int ty) {
        this.targetX = tx;
        this.targetY = ty;
        this.x = tx - 40; // Offset agar center (asumsi gambar 80x80)
        this.y = -100;    // Mulai dari atas layar
    }

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
