/**
 * Copyright (c) 2025 Muhammad 'Azmi Salam. All Rights Reserved.
 * Email: mhmmdzmslm36@gmail.com
 * GitHub: https://github.com/zicofarry
 */
package com.lastpenguin.model;

import java.awt.Rectangle;

/**
 * Represents the main Penguin character.
 * Handles movement boundaries, ammunition scavenging logic, and skill states.
 * @author Muhammad 'Azmi Salam
 * @version 1.6
 */
public class Player {
    private String username;
    private int x, y, score, remainingBullets, yetiKilled, missedShots;
    private int lastDx = 0, lastDy = -1; // Default menghadap ke atas
    private boolean alive = true;
    
    // Skill & Buff Variables
    private int cooldownS1 = 0, cooldownS2 = 0, cooldownS3 = 0;
    private int s1RemainingShots = 0; // Melacak sisa peluru Giant Snowball
    private int ghostDuration = 0;    // Melacak durasi Invisible

    public Player(String username, int startingBullets) {
        this.username = username;
        this.x = 375; // Posisi awal tengah
        this.y = 200;
        this.remainingBullets = startingBullets; 
        this.score = 0;
        this.yetiKilled = 0;
        this.missedShots = 0;
    }

    /**
     * Menggerakkan player dan menjaga agar tetap di dalam batas arena.
     */
    public void move(int dx, int dy) {
        if (!alive) return;
        this.x += dx * 5;
        this.y += dy * 5;

        // Simpan arah hadap terakhir jika sedang bergerak
        if (dx != 0 || dy != 0) {
            this.lastDx = dx;
            this.lastDy = dy;
        }

        // Boundary checks (Arena 800x600)
        if (x < 0) x = 0;
        if (x > 750) x = 750;
        if (y < 0) y = 0;
        if (y > 550) y = 550;
    }

    /**
     * Mendapatkan kotak tabrakan (collision) player.
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, 40, 40);
    }

    // --- State Management ---
    
    public void die() { this.alive = false; }
    public boolean isAlive() { return alive; }
    
    /**
     * Mengecek apakah buff Giant Snowball aktif.
     * Digunakan untuk menentukan apakah vfx/buff_skill1.png harus digambar.
     */
    public boolean isGiantBuffActive() {
        return s1RemainingShots > 0;
    }

    public void updateTimers() {
        // Update Cooldown Skill
        if (cooldownS1 > 0) cooldownS1--;
        if (cooldownS2 > 0) cooldownS2--;
        if (cooldownS3 > 0) cooldownS3--;
        
        // Update Durasi Buff Aktif
        if (ghostDuration > 0) ghostDuration--;
    }

    // --- Ammunition & Scoring ---

    public void addBullets(int count) { this.remainingBullets += count; }
    
    public void useBullet() { 
        if (remainingBullets > 0) remainingBullets--; 
    }
    
    public void registerMiss() { this.missedShots++; }
    
    public void registerKill(int points) { 
        this.score += points; 
        this.yetiKilled++;
    }

    // --- Getters & Setters ---

    public int getX() { return x; }
    public int getY() { return y; }
    public int getScore() { return score; }
    public int getRemainingBullets() { return remainingBullets; }
    public int getYetiKilled() { return yetiKilled; }
    public int getMissedShots() { return missedShots; }
    public String getUsername() { return username; }
    public int getHealth() { return alive ? 100 : 0; }
    public int getLastDx() { return lastDx; }
    public int getLastDy() { return lastDy; }

    // Skill 1: Giant Snowball
    public int getCooldownS1() { return cooldownS1; }
    public void setCooldownS1(int v) { this.cooldownS1 = v; }
    public int getS1RemainingShots() { return s1RemainingShots; }
    public void setS1RemainingShots(int v) { this.s1RemainingShots = v; }
    
    /**
     * Memanggil ini setiap kali peluru giant ditembakkan.
     * Jika sudah mencapai 3 tembakan, buff akan hilang otomatis.
     */
    public void useS1Shot() { 
        if (s1RemainingShots > 0) {
            s1RemainingShots--; 
        }
    }

    // Skill 2: Meteor Rain
    public int getCooldownS2() { return cooldownS2; }
    public void setCooldownS2(int v) { this.cooldownS2 = v; }

    // Skill 3: Invisible
    public int getCooldownS3() { return cooldownS3; }
    public void setCooldownS3(int v) { this.cooldownS3 = v; }
    public boolean isGhost() { return ghostDuration > 0; }
    public void setGhostDuration(int v) { this.ghostDuration = v; }
}