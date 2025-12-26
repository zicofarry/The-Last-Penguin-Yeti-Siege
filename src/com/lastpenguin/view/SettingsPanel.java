package com.lastpenguin.view;

import com.lastpenguin.model.GameSettings;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class SettingsPanel extends JPanel {

    private JComboBox<String> diffBox;
    private JComboBox<String> modeBox;
    private JCheckBox musicToggle, sfxToggle, mouseCheck;
    private JButton btnKeyS1, btnKeyS2, btnKeyS3;
    private int tempKeyS1, tempKeyS2, tempKeyS3;
    private Runnable onChange; // Callback untuk autosave

    public SettingsPanel(GameSettings current, boolean isIngame, Runnable onChange, ActionListener backAction) {
        this.onChange = onChange;
        setLayout(new GridLayout(9, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(30, 80, 30, 80));
        setBackground(new Color(220, 240, 255));

        tempKeyS1 = current.getKeyS1();
        tempKeyS2 = current.getKeyS2();
        tempKeyS3 = current.getKeyS3();

        // Difficulty & Mode
        diffBox = new JComboBox<>(new String[]{GameSettings.EASY, GameSettings.MEDIUM, GameSettings.HARD});
        diffBox.setSelectedItem(current.getDifficulty());
        diffBox.addActionListener(e -> onChange.run()); // Autosave saat ganti

        modeBox = new JComboBox<>(new String[]{GameSettings.OFFLINE, GameSettings.ONLINE});
        modeBox.setSelectedItem(current.getMode());
        modeBox.addActionListener(e -> onChange.run()); // Autosave saat ganti

        if (isIngame) {
            diffBox.setEnabled(false);
            modeBox.setEnabled(false);
        }

        // Toggles
        musicToggle = new JCheckBox("Music ON", current.getMusicVolume() > 0);
        musicToggle.setBackground(new Color(220, 240, 255));
        musicToggle.addActionListener(e -> onChange.run()); // Autosave + Music Trigger

        sfxToggle = new JCheckBox("SFX ON", current.getSfxVolume() > 0);
        sfxToggle.setBackground(new Color(220, 240, 255));
        sfxToggle.addActionListener(e -> onChange.run());

        mouseCheck = new JCheckBox("Enable Mouse Aim & Shoot", current.isUseMouse());
        mouseCheck.setBackground(new Color(220, 240, 255));
        mouseCheck.addActionListener(e -> onChange.run());

        // Keys
        btnKeyS1 = createKeyButton(tempKeyS1, 1);
        btnKeyS2 = createKeyButton(tempKeyS2, 2);
        btnKeyS3 = createKeyButton(tempKeyS3, 3);

        // Tombol BACK saja (tidak ada logika save di sini)
        JButton btnBack = new JButton("BACK");
        btnBack.setFont(new Font("Arial", Font.BOLD, 14));
        btnBack.setBackground(new Color(150, 150, 150));
        btnBack.addActionListener(backAction);

        add(new JLabel("Game Difficulty:")); add(diffBox);
        add(new JLabel("Game Mode:")); add(modeBox);
        add(new JLabel("Background Music:")); add(musicToggle);
        add(new JLabel("Sound Effects:")); add(sfxToggle);
        add(new JLabel("Mouse Control:")); add(mouseCheck);
        add(new JLabel("Skill 1 Key:")); add(btnKeyS1);
        add(new JLabel("Skill 2 Key:")); add(btnKeyS2);
        add(new JLabel("Skill 3 Key:")); add(btnKeyS3);
        add(new JLabel("")); add(btnBack);
    }

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
                if (skillNum == 1) tempKeyS1 = newKey;
                if (skillNum == 2) tempKeyS2 = newKey;
                if (skillNum == 3) tempKeyS3 = newKey;
                btn.setText(KeyEvent.getKeyText(newKey));
                btn.setBackground(null);
                SettingsPanel.this.requestFocusInWindow();
                onChange.run(); // Autosave setelah ganti tombol
            }
        });
        return btn;
    }

    public String getSelectedDifficulty() { return (String) diffBox.getSelectedItem(); }
    public String getSelectedMode() { return (String) modeBox.getSelectedItem(); }
    public boolean isMusicEnabled() { return musicToggle.isSelected(); }
    public boolean isSfxEnabled() { return sfxToggle.isSelected(); }
    public boolean isMouseEnabled() { return mouseCheck.isSelected(); }
    public int getS1Key() { return tempKeyS1; }
    public int getS2Key() { return tempKeyS2; }
    public int getS3Key() { return tempKeyS3; }
}