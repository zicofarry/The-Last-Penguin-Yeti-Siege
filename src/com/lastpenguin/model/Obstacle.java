/**
 * Copyright (c) 2025 Muhammad 'Azmi Salam. All Rights Reserved.
 * Email: mhmmdzmslm36@gmail.com
 * GitHub: https://github.com/zicofarry
 */
package com.lastpenguin.model;

/**
 * Merepresentasikan objek lingkungan statis seperti batu es atau tembok salju.
 * Obstacle menghalangi pergerakan entitas dan menghancurkan proyektil.
 * Versi ini mendukung sistem HP di mana rintangan dapat hancur dan berubah bentuk.
 * * @author Muhammad 'Azmi Salam
 * @version 1.1
 * @since December 2025
 */
public class Obstacle {
    private int x, y;
    private int width, height;
    private boolean isHole = false;
    private int duration = -1;
    
    // Fitur baru: Durabilitas rintangan
    private int hp = -1; 
    private boolean destructible = false;

    /**
     * Konstruktor standar untuk obstacle statis (indestructible).
     */
    public Obstacle(int x, int y, int width, int height) {
        this.x = x; 
        this.y = y; 
        this.width = width; 
        this.height = height;
    }

    /**
     * Konstruktor baru untuk rintangan yang bisa hancur (Batu/Duri).
     * @param initialHp HP awal (30 untuk Batu, 15 untuk Duri).
     */
    public Obstacle(int x, int y, int width, int height, int initialHp) {
        this(x, y, width, height);
        this.hp = initialHp;
        this.destructible = true;
    }

    /**
     * Konstruktor untuk lubang (hasil skill Meteor).
     */
    public Obstacle(int x, int y, int width, int height, boolean isHole, int duration) {
        this(x, y, width, height);
        this.isHole = isHole;
        this.duration = duration;
    }

    /**
     * Mengurangi HP rintangan saat terkena proyektil.
     */
    public void takeDamage() {
        if (destructible && hp > 0) {
            hp--;
        }
    }

    /**
     * Update durasi untuk rintangan tipe lubang.
     */
    public void update() { 
        if (duration > 0) duration--; 
    }

    /**
     * Mengecek apakah lubang sudah waktunya menghilang.
     */
    public boolean isExpired() { 
        return isHole && duration == 0; 
    }

    /**
     * Mengecek apakah rintangan es sudah hancur sepenuhnya.
     */
    public boolean isDestroyed() {
        return destructible && hp <= 0;
    }

    // Getters
    public boolean isHole() { return isHole; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getHp() { return hp; }
    public boolean isDestructible() { return destructible; }
}