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

    // UPDATE: Konstruktor sekarang menerima 3 parameter
    public SettingsPanel(GameSettings current, boolean isIngame, ActionListener backAction) {
        setLayout(new GridLayout(9, 2, 10, 10)); // Ubah ke 9 baris untuk SFX
        setBorder(BorderFactory.createEmptyBorder(30, 80, 30, 80));
        setBackground(new Color(220, 240, 255));

        tempKeyS1 = current.getKeyS1();
        tempKeyS2 = current.getKeyS2();
        tempKeyS3 = current.getKeyS3();

        diffBox = new JComboBox<>(new String[]{GameSettings.EASY, GameSettings.MEDIUM, GameSettings.HARD});
        diffBox.setSelectedItem(current.getDifficulty());

        modeBox = new JComboBox<>(new String[]{GameSettings.OFFLINE, GameSettings.ONLINE});
        modeBox.setSelectedItem(current.getMode());

        // LOGIKA: Matikan pilihan jika sedang di dalam game
        if (isIngame) {
            diffBox.setEnabled(false);
            modeBox.setEnabled(false);
        }

        musicToggle = new JCheckBox("Music ON", current.getMusicVolume() > 0);
        musicToggle.setBackground(new Color(220, 240, 255));
        
        sfxToggle = new JCheckBox("SFX ON", current.getSfxVolume() > 0);
        sfxToggle.setBackground(new Color(220, 240, 255));
        
        mouseCheck = new JCheckBox("Enable Mouse Aim & Shoot", current.isUseMouse());
        mouseCheck.setBackground(new Color(220, 240, 255));

        btnKeyS1 = createKeyButton(tempKeyS1, 1);
        btnKeyS2 = createKeyButton(tempKeyS2, 2);
        btnKeyS3 = createKeyButton(tempKeyS3, 3);

        JButton btnSave = new JButton("SAVE & BACK");
        btnSave.setFont(new Font("Arial", Font.BOLD, 14));
        btnSave.setBackground(new Color(100, 200, 100));
        btnSave.addActionListener(backAction);

        add(new JLabel("Game Difficulty:")); add(diffBox);
        add(new JLabel("Game Mode:")); add(modeBox);
        add(new JLabel("Background Music:")); add(musicToggle);
        add(new JLabel("Sound Effects:")); add(sfxToggle);
        add(new JLabel("Mouse Control:")); add(mouseCheck);
        add(new JLabel("Skill 1 Key:")); add(btnKeyS1);
        add(new JLabel("Skill 2 Key:")); add(btnKeyS2);
        add(new JLabel("Skill 3 Key:")); add(btnKeyS3);
        add(new JLabel("")); add(btnSave);
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