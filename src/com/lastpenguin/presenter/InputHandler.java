package com.lastpenguin.presenter;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import com.lastpenguin.model.GameSettings;

/**
 * Manages all user input for the application.
 * This class captures and stores the state of keyboard keys and mouse actions, 
 * allowing the game logic to respond to movement, combat, and menu commands.
 */
public class InputHandler extends KeyAdapter implements MouseListener, MouseMotionListener {
    private boolean up, down, left, right, shooting, paused;
    private boolean s1, s2, s3;
    private int mouseX, mouseY;
    private boolean mouseClicked;
    private GameSettings settings;

    /**
     * Integrates game configurations to support custom keybindings.
     */
    public void setSettings(GameSettings settings) { this.settings = settings; }

    /**
     * Handles keyboard press events.
     * Maps physical keys to game actions such as movement, special skills, 
     * and pausing the game session.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        
        // Directional movement mapping
        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) up = true;
        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) down = true;
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) left = true;
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) right = true;
        
        // Combat and system actions
        if (code == KeyEvent.VK_X) shooting = true;
        if (code == KeyEvent.VK_1) s1 = true;
        if (code == KeyEvent.VK_2) s2 = true;
        if (code == KeyEvent.VK_3) s3 = true;
        if (code == KeyEvent.VK_SPACE) paused = !paused;
        
        // Support for customized keybindings from settings
        if (settings != null) {
            if (code == settings.getKeyS1()) s1 = true;
            if (code == settings.getKeyS2()) s2 = true;
            if (code == settings.getKeyS3()) s3 = true;
        }
    }

    /**
     * Handles keyboard release events to stop ongoing actions.
     */
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

    // --- Mouse Event Implementations ---

    /**
     * Records mouse button press for targeting or shooting.
     */
    @Override
    public void mousePressed(MouseEvent e) { mouseClicked = true; }
    
    /**
     * Records mouse button release.
     */
    @Override
    public void mouseReleased(MouseEvent e) { mouseClicked = false; }
    
    /**
     * Updates current mouse coordinates relative to the game window.
     */
    @Override
    public void mouseMoved(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
    
    /**
     * Tracks mouse position during dragging operations.
     */
    @Override
    public void mouseDragged(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }

    // Required empty implementations for the MouseListener interface
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // --- Getter Methods for Game Engine Synchronization ---
    
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