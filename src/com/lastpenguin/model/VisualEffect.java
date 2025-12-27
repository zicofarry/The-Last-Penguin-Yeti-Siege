package com.lastpenguin.model;

public class VisualEffect {
    private int x, y;
    private int currentFrame;
    private int maxFrames;
    private boolean active;
    private String type; // Tambahan: Untuk membedakan jenis efek

    public VisualEffect(int x, int y, int duration, String type) {
        this.x = x;
        this.y = y;
        this.maxFrames = duration;
        this.currentFrame = 0;
        this.active = true;
        this.type = type;
    }

    public void update() {
        if (!active) return;
        currentFrame++;
        if (currentFrame >= maxFrames) active = false;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public String getType() { return type; }
    public boolean isActive() { return active; }
    public float getOpacity() { return 1.0f - ((float)currentFrame / maxFrames); }
}