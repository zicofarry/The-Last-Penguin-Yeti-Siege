/**
 * Copyright (c) 2025 Muhammad 'Azmi Salam. All Rights Reserved.
 * Email: mhmmdzmslm36@gmail.com
 * GitHub: https://github.com/zicofarry
 */
package com.lastpenguin.view;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.net.URL;

/**
 * Utility class for managing game sounds and background music.
 * * @author Muhammad 'Azmi Salam
 * @version 1.0
 * @since December 2025
 */
public class Sound {
    private Clip clip;

    /**
     * Plays a sound file once (useful for SFX like shots or hits).
     * @param fileName Name of the file in /res/assets/sounds/
     */
    public void playEffect(String fileName) {
        try {
            URL url = getClass().getResource("/assets/sounds/" + fileName);
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Clip sfx = AudioSystem.getClip();
            sfx.open(ais);
            sfx.start();
        } catch (Exception e) {
            System.err.println("Error playing SFX: " + e.getMessage());
        }
    }

    /**
     * Plays music in a continuous loop.
     * @param fileName Name of the file in /res/assets/sounds/
     */
    public void playMusic(String fileName) {
        try {
            URL url = getClass().getResource("/assets/sounds/" + fileName);
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(ais);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        } catch (Exception e) {
            System.err.println("Error playing music: " + e.getMessage());
        }
    }

    /**
     * Stops the currently playing music loop.
     */
    public void stopMusic() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }
}
