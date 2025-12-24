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
    private static final Map<String, BufferedImage> imageCache = new HashMap<>();

    /**
     * Loads an image from the resources folder.
     * @param fileName The name of the file inside /res/assets/images/
     * @return The loaded BufferedImage or null if failed.
     */
    public static BufferedImage loadImage(String fileName) {
        String path = "/assets/" + fileName;
        
        // Return from cache if already loaded
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

        try (InputStream is = AssetLoader.class.getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("Resource not found: " + path);
                return null;
            }
            BufferedImage image = ImageIO.read(is);
            imageCache.put(path, image);
            return image;
        } catch (IOException e) {
            System.err.println("Error loading image [" + path + "]: " + e.getMessage());
            return null;
        }
    }

    /**
     * Clears the image cache to free up memory if needed.
     */
    public static void clearCache() {
        imageCache.clear();
    }
}
