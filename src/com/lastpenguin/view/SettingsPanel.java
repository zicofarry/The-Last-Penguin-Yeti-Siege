/**
 * Copyright (c) 2025 Muhammad 'Azmi Salam. All Rights Reserved.
 * Email: mhmmdzmslm36@gmail.com
 * GitHub: https://github.com/zicofarry
 */
package com.lastpenguin.view;

import com.lastpenguin.model.GameSettings;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * UI for modifying game preferences including difficulty, mode, volume, 
 * and custom controller settings.
 */
public class SettingsPanel extends JPanel {

    private JComboBox<String> diffBox;
    private JComboBox<String> modeBox;
    private JSlider volumeSlider;
    private JCheckBox mouseCheck;
    
    // Tombol untuk mengatur skill
    private JButton btnKeyS1, btnKeyS2, btnKeyS3;
    private int tempKeyS1, tempKeyS2, tempKeyS3;

    public SettingsPanel(GameSettings current, ActionListener backAction) {
        // Gunakan 8 baris (Difficulty, Mode, Vol, Mouse, S1, S2, S3, Save)
        setLayout(new GridLayout(8, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(30, 80, 30, 80));
        setBackground(new Color(220, 240, 255));

        // 1. Inisialisasi Nilai Awal
        tempKeyS1 = current.getKeyS1();
        tempKeyS2 = current.getKeyS2();
        tempKeyS3 = current.getKeyS3();

        // 2. Komponen Dasar
        diffBox = new JComboBox<>(new String[]{GameSettings.EASY, GameSettings.MEDIUM, GameSettings.HARD});
        diffBox.setSelectedItem(current.getDifficulty());

        modeBox = new JComboBox<>(new String[]{GameSettings.OFFLINE, GameSettings.ONLINE});
        modeBox.setSelectedItem(current.getMode());

        volumeSlider = new JSlider(0, 100, current.getMusicVolume());
        
        mouseCheck = new JCheckBox("Enable Mouse Aim & Shoot", current.isUseMouse());
        mouseCheck.setBackground(new Color(220, 240, 255));

        // 3. Setup Tombol Controller
        btnKeyS1 = createKeyButton(tempKeyS1, 1);
        btnKeyS2 = createKeyButton(tempKeyS2, 2);
        btnKeyS3 = createKeyButton(tempKeyS3, 3);

        JButton btnSave = new JButton("SAVE & BACK");
        btnSave.setFont(new Font("Arial", Font.BOLD, 14));
        btnSave.setBackground(new Color(100, 200, 100));
        btnSave.addActionListener(backAction);

        // 4. Tambahkan ke Panel
        add(new JLabel("Game Difficulty:"));
        add(diffBox);
        
        add(new JLabel("Game Mode:"));
        add(modeBox);
        
        add(new JLabel("Music Volume:"));
        add(volumeSlider);

        add(new JLabel("Mouse Control:"));
        add(mouseCheck);

        add(new JLabel("Skill 1 Key:"));
        add(btnKeyS1);

        add(new JLabel("Skill 2 Key:"));
        add(btnKeyS2);

        add(new JLabel("Skill 3 Key:"));
        add(btnKeyS3);

        add(new JLabel("")); // Spacer
        add(btnSave);
    }

    /**
     * Membuat tombol yang bisa mendengarkan input keyboard untuk rebind key.
     */
    private JButton createKeyButton(int initialKey, int skillNum) {
        JButton btn = new JButton(KeyEvent.getKeyText(initialKey));
        
        btn.addActionListener(e -> {
            btn.setText("... PRESS ANY KEY ...");
            btn.setBackground(Color.YELLOW);
            btn.requestFocusInWindow();
        });

        btn.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int newKey = e.getKeyCode();
                // Update variabel temp berdasarkan skill yang mana
                if (skillNum == 1) tempKeyS1 = newKey;
                if (skillNum == 2) tempKeyS2 = newKey;
                if (skillNum == 3) tempKeyS3 = newKey;
                
                btn.setText(KeyEvent.getKeyText(newKey));
                btn.setBackground(null); // Reset warna
                SettingsPanel.this.requestFocusInWindow(); // Pindahkan fokus kembali
            }
        });

        return btn;
    }

    // --- GETTER UNTUK DIAMBIL OLEH MAIN.JAVA SAAT SAVE ---
    public String getSelectedDifficulty() { return (String) diffBox.getSelectedItem(); }
    public String getSelectedMode() { return (String) modeBox.getSelectedItem(); }
    public int getVolume() { return volumeSlider.getValue(); }
    public boolean isMouseEnabled() { return mouseCheck.isSelected(); }
    
    public int getS1Key() { return tempKeyS1; }
    public int getS2Key() { return tempKeyS2; }
    public int getS3Key() { return tempKeyS3; }
}