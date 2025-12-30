package com.lastpenguin.view;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for resource management and asset retrieval.
 * Provides streamlined methods for loading images and sprite arrays 
 * from the local resource path.
 */
public class AssetLoader {

    private static final String IMAGE_PATH = "/assets/images/";
    private static final String SOUND_PATH = "/assets/sounds/";

    /**
     * Loads a single image file from the embedded resources.
     * @param fileName The relative path of the file within the asset directory.
     * @return The loaded BufferedImage, or null if retrieval fails.
     */
    public static BufferedImage loadImage(String fileName) {
        try {
            InputStream is = AssetLoader.class.getResourceAsStream(IMAGE_PATH + fileName);
            if (is == null) {
                System.err.println("Resource not found: " + IMAGE_PATH + fileName);
                return null;
            }
            return ImageIO.read(is);
        } catch (IOException e) {
            System.err.println("Error reading image " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Aggregates and loads all animation frames for the Yeti entity.
     * Uses a fallback mechanism to prevent visual errors if specific frames are missing.
     * @return An array of BufferedImage containing directional and walking frames.
     */
    public static BufferedImage[] loadYetiSprites() {
        BufferedImage[] sprites = new BufferedImage[12];
        String[] directions = {"front", "left", "right", "back"};
        String[] steps = {"left", "balanced", "right"};
        
        int index = 0;
        for (String dir : directions) {
            for (String step : steps) {
                String fileName = "sprites/yeti_" + dir + "_" + step + ".png";
                BufferedImage img = loadImage(fileName);
                
                if (img == null) {
                    img = loadImage("sprites/yeti_front_balanced.png");
                }

                if (img == null) img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                sprites[index++] = img;
            }
        }
        return sprites;
    }

    /**
     * Retrieves an input stream for audio files.
     */
    public static InputStream getSoundStream(String fileName) {
        return AssetLoader.class.getResourceAsStream(SOUND_PATH + fileName);
    }
}