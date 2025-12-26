// File: src/com/lastpenguin/view/Sound.java
package com.lastpenguin.view;

import com.lastpenguin.model.GameSettings;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public class Sound {
    private Clip musicClip;
    private GameSettings settings;

    // Tambahkan method untuk menghubungkan settings
    public void setSettings(GameSettings settings) {
        this.settings = settings;
    }

    public void playEffect(String fileName) {
        // Cek apakah SFX Volume > 0 (Artinya ON)
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

    public void playMusic(String fileName) {
        // Pastikan musik yang lama berhenti dulu sebelum cek pengaturan
        stopMusic(); 

        // Jika pengaturan musik OFF (volume <= 0), maka berhenti di sini
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

    public void stopMusic() {
        if (musicClip != null && musicClip.isRunning()) {
            musicClip.stop();
            musicClip.close();
        }
    }
}