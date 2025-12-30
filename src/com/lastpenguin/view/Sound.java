// File: src/com/lastpenguin/view/Sound.java
package com.lastpenguin.view;

import com.lastpenguin.model.GameSettings;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

/**
 * Handles audio playback for the application.
 * This class manages both short-duration sound effects and continuous 
 * background music, integrating directly with user settings to control 
 * playback availability.
 */
public class Sound {
    private Clip musicClip;
    private GameSettings settings;

    /**
     * Associates the game settings with the sound manager to enable 
     * volume-based playback control.
     */
    public void setSettings(GameSettings settings) {
        this.settings = settings;
    }

    /**
     * Plays a specific sound effect file.
     * Playback is automatically bypassed if the SFX volume is disabled 
     * within the game settings.
     */
    public void playEffect(String fileName) {
        // Validates if SFX playback is enabled based on user configurations
        if (settings != null && settings.getSfxVolume() <= 0) return;

        try {
            URL url = getClass().getResource("/assets/sounds/" + fileName);
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Clip sfx = AudioSystem.getClip();
            sfx.open(ais);
            sfx.start();
        } catch (Exception e) {
            System.err.println("SFX Error: " + fileName + " - " + e.getMessage());
        }
    }

    /**
     * Initiates background music playback in a continuous loop.
     * Ensures any currently playing music is terminated before starting a new track, 
     * provided the music setting is enabled.
     */
    public void playMusic(String fileName) {
        // Ensures previous music instances are stopped before checking new settings
        stopMusic(); 

        // Prevents playback if music is disabled in the game settings
        if (settings != null && settings.getMusicVolume() <= 0) return;

        try {
            URL url = getClass().getResource("/assets/sounds/" + fileName);
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            musicClip = AudioSystem.getClip();
            musicClip.open(ais);
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            musicClip.start();
        } catch (Exception e) {
            System.err.println("Music Error: " + e.getMessage());
        }
    }

    /**
     * Stops the currently active background music and releases system resources.
     */
    public void stopMusic() {
        if (musicClip != null && musicClip.isRunning()) {
            musicClip.stop();
            musicClip.close();
        }
    }
}