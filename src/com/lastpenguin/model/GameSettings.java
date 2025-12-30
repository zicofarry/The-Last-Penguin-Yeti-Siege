package com.lastpenguin.model;
import java.awt.event.KeyEvent;

/**
 * Data transfer object that encapsulates global game configurations, 
 * control preferences, and audio settings.
 */
public class GameSettings {
    
    public static final String EASY = "EASY";
    public static final String MEDIUM = "MEDIUM";
    public static final String HARD = "HARD";

    public static final String OFFLINE = "OFFLINE";
    public static final String ONLINE = "ONLINE";

    private int keyS1 = KeyEvent.VK_1;
    private int keyS2 = KeyEvent.VK_2;
    private int keyS3 = KeyEvent.VK_3;
    private boolean useMouse = true;

    private int musicVolume;
    private int sfxVolume;
    private String difficulty;
    private String mode;

    /**
     * Default constructor for initial application startup.
     */
    public GameSettings() {
        this.musicVolume = 50;
        this.sfxVolume = 50;
        this.difficulty = EASY;
        this.mode = OFFLINE;
    }

    /**
     * Parameterized constructor used when loading existing settings from the database.
     */
    public GameSettings(int musicVolume, int sfxVolume, String difficulty, String mode) {
        this.musicVolume = musicVolume;
        this.sfxVolume = sfxVolume;
        this.difficulty = difficulty;
        this.mode = mode;
    }

    public int getMusicVolume() { return musicVolume; }
    public void setMusicVolume(int musicVolume) { this.musicVolume = musicVolume; }

    public int getSfxVolume() { return sfxVolume; }
    public void setSfxVolume(int sfxVolume) { this.sfxVolume = sfxVolume; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public int getKeyS1() { return keyS1; }
    public void setKeyS1(int key) { this.keyS1 = key; }
    public int getKeyS2() { return keyS2; }
    public void setKeyS2(int key) { this.keyS2 = key; }
    public int getKeyS3() { return keyS3; }
    public void setKeyS3(int key) { this.keyS3 = key; }
    public boolean isUseMouse() { return useMouse; }
    public void setUseMouse(boolean useMouse) { this.useMouse = useMouse; }
}