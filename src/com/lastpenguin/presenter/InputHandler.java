package com.lastpenguin.presenter;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import com.lastpenguin.model.GameSettings;

public class InputHandler extends KeyAdapter implements MouseListener, MouseMotionListener {
    private boolean up, down, left, right, shooting, paused;
    private boolean s1, s2, s3;
    private int mouseX, mouseY;
    private boolean mouseClicked;
    private GameSettings settings;

    public void setSettings(GameSettings settings) { this.settings = settings; }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) up = true;
        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) down = true;
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) left = true;
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) right = true;
        if (code == KeyEvent.VK_X) shooting = true;
        if (code == KeyEvent.VK_1) s1 = true;
        if (code == KeyEvent.VK_2) s2 = true;
        if (code == KeyEvent.VK_3) s3 = true;
        if (code == KeyEvent.VK_SPACE) paused = !paused;
        if (settings != null) {
            if (code == settings.getKeyS1()) s1 = true;
            if (code == settings.getKeyS2()) s2 = true;
            if (code == settings.getKeyS3()) s3 = true;
        }
        
        if (code == KeyEvent.VK_SPACE) {
            paused = !paused;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) up = false;
        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) down = false;
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) left = false;
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) right = false;
        if (code == KeyEvent.VK_X) shooting = false;
        if (code == KeyEvent.VK_1) s1 = false;
        if (code == KeyEvent.VK_2) s2 = false;
        if (code == KeyEvent.VK_3) s3 = false;
    }

    // IMPLEMENTASI MOUSE LISTENER (Wajib ada semua)
    @Override
    public void mousePressed(MouseEvent e) { mouseClicked = true; }
    @Override
    public void mouseReleased(MouseEvent e) { mouseClicked = false; }
    @Override
    public void mouseMoved(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
    @Override
    public void mouseDragged(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }

    // Method kosong agar tidak error
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // Getters
    public boolean isUp() { return up; }
    public boolean isDown() { return down; }
    public boolean isLeft() { return left; }
    public boolean isRight() { return right; }
    public boolean isShooting() { return shooting; }
    public boolean isPaused() { return paused; }
    public boolean isS1() { return s1; }
    public boolean isS2() { return s2; }
    public boolean isS3() { return s3; }
    public int getMouseX() { return mouseX; }
    public int getMouseY() { return mouseY; }
    public boolean isMouseClicked() { return mouseClicked; }
    public void setPaused(boolean paused) { this.paused = paused; }
}