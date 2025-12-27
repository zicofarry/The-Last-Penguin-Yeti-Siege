/**
 * Copyright (c) 2025 Muhammad 'Azmi Salam. All Rights Reserved.
 * Email: mhmmdzmslm36@gmail.com
 * GitHub: https://github.com/zicofarry
 */
package com.lastpenguin.view;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for loading and caching game assets such as images and sprites.
 * This ensures that resources are only loaded once into memory for better performance.
 * * @author Muhammad 'Azmi Salam
 * @version 1.0
 * @since December 2025
 */
public class AssetLoader {

    // Cache to store loaded images
    private static final String IMAGE_PATH = "/assets/images/";
    private static final String SOUND_PATH = "/assets/sounds/";

    /**
     * Loads an image from the resources folder.
     * @param fileName The name of the file inside /res/assets/images/
     * @return The loaded BufferedImage or null if failed.
     */
    public static BufferedImage loadImage(String fileName) {
        try {
            // Mengambil resource menggunakan ClassLoader agar kompatibel saat dibungkus ke .jar
            InputStream is = AssetLoader.class.getResourceAsStream(IMAGE_PATH + fileName);
            if (is == null) {
                System.err.println("Gagal menemukan gambar: " + IMAGE_PATH + fileName);
                return null;
            }
            return ImageIO.read(is);
        } catch (IOException e) {
            System.err.println("Eror saat membaca gambar " + fileName + ": " + e.getMessage());
            return null;
        }
    }
    // Tambahkan di AssetLoader.java
    public static BufferedImage[] loadYetiSprites() {
        BufferedImage[] sprites = new BufferedImage[12];
        String[] directions = {"front", "left", "right", "back"};
        String[] steps = {"left", "balanced", "right"};
        
        int index = 0;
        for (String dir : directions) {
            for (String step : steps) {
                String fileName = "sprites/yeti_" + dir + "_" + step + ".png";
                BufferedImage img = loadImage(fileName);
                
                // Fallback: Jika gambar tidak ditemukan, gunakan yeti_front_balanced.png (no. 2)
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
     * (Placeholder) Untuk memuat input stream suara dari res/assets/sounds/
     */
    public static InputStream getSoundStream(String fileName) {
        return AssetLoader.class.getResourceAsStream(SOUND_PATH + fileName);
    }
}
