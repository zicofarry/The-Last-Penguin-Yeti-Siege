package com.lastpenguin.view;

import com.lastpenguin.model.GameSettings;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

/**
 * Handles the graphical settings interface.
 * Provides controls for game mechanics, audio preferences, and custom keybindings
 * while ensuring all modifications are synchronized with the GameSettings model.
 */
public class SettingsPanel extends JPanel {

    private Image backgroundImage;
    private Font customFont;
    private Runnable onChange;
    private Sound soundManager = new Sound();

    private JComboBox<String> diffBox, modeBox;
    private JButton musicBtn, sfxBtn, mouseBtn;
    private JButton btnKeyS1, btnKeyS2, btnKeyS3;
    
    private boolean musicOn, sfxOn, mouseOn;
    private int tempKeyS1, tempKeyS2, tempKeyS3;

    public SettingsPanel(GameSettings current, boolean isIngame, Runnable onChange, ActionListener backAction) {
        this.onChange = onChange;
        this.setLayout(null);

        loadResources();

        // Synchronize local state with the current game configurations
        musicOn = current.getMusicVolume() > 0;
        sfxOn = current.getSfxVolume() > 0;
        mouseOn = current.isUseMouse();
        tempKeyS1 = current.getKeyS1();
        tempKeyS2 = current.getKeyS2();
        tempKeyS3 = current.getKeyS3();

        // Coordinate definitions for component alignment within the background assets
        int labelX = 200; 
        int controlX = 440; 
        int startY = 150; 
        int gapY = 40; 

        // --- ROW 1: DIFFICULTY CONFIGURATION ---
        addLabel("Difficulty:", labelX, startY);
        diffBox = new JComboBox<>(new String[]{GameSettings.EASY, GameSettings.MEDIUM, GameSettings.HARD});
        diffBox.setSelectedItem(current.getDifficulty());
        diffBox.setBounds(controlX, startY, 170, 30);
        styleComboBox(diffBox); 
        diffBox.addActionListener(e -> {
            soundManager.playEffect("sfx_click.wav");
            onChange.run();
        });
        if (isIngame) diffBox.setEnabled(false);
        add(diffBox);

        // --- ROW 2: GAME MODE SELECTION ---
        addLabel("Game Mode:", labelX, startY + gapY);
        modeBox = new JComboBox<>(new String[]{GameSettings.OFFLINE, GameSettings.ONLINE});
        modeBox.setSelectedItem(current.getMode());
        modeBox.setBounds(controlX, startY + gapY, 170, 30);
        styleComboBox(modeBox); 
        modeBox.addActionListener(e -> {
            soundManager.playEffect("sfx_click.wav");
            onChange.run();
        });
        if (isIngame) modeBox.setEnabled(false);
        add(modeBox);

        // --- ROW 3, 4, 5: AUDIO AND CONTROL TOGGLES ---
        addLabel("Music:", labelX, startY + (gapY * 2));
        musicBtn = createToggleButton(musicOn, controlX, startY + (gapY * 2));
        musicBtn.addActionListener(e -> {
            soundManager.playEffect("sfx_click.wav");
            musicOn = !musicOn;
            musicBtn.setText(musicOn ? "ON" : "OFF");
            onChange.run();
        });
        add(musicBtn);

        addLabel("Sound FX:", labelX, startY + (gapY * 3));
        sfxBtn = createToggleButton(sfxOn, controlX, startY + (gapY * 3));
        sfxBtn.addActionListener(e -> {
            soundManager.playEffect("sfx_click.wav");
            sfxOn = !sfxOn;
            sfxBtn.setText(sfxOn ? "ON" : "OFF");
            onChange.run();
        });
        add(sfxBtn);

        addLabel("Mouse Aim:", labelX, startY + (gapY * 4));
        mouseBtn = createToggleButton(mouseOn, controlX, startY + (gapY * 4));
        mouseBtn.addActionListener(e -> {
            soundManager.playEffect("sfx_click.wav");
            mouseOn = !mouseOn;
            mouseBtn.setText(mouseOn ? "ON" : "OFF");
            onChange.run();
        });
        add(mouseBtn);

        // --- ROW 6, 7, 8: CUSTOM KEYBINDINGS ---
        addLabel("Skill 1 Key:", labelX, startY + (gapY * 5));
        btnKeyS1 = createKeyButton(tempKeyS1, 1, controlX, startY + (gapY * 5));
        add(btnKeyS1);

        addLabel("Skill 2 Key:", labelX, startY + (gapY * 6));
        btnKeyS2 = createKeyButton(tempKeyS2, 2, controlX, startY + (gapY * 6));
        add(btnKeyS2);

        addLabel("Skill 3 Key:", labelX, startY + (gapY * 7));
        btnKeyS3 = createKeyButton(tempKeyS3, 3, controlX, startY + (gapY * 7));
        add(btnKeyS3);

        // --- NAVIGATION CONTROLS ---
        JButton btnBack = new JButton("");
        btnBack.setFont(customFont.deriveFont(Font.BOLD, 22f));
        btnBack.setForeground(Color.WHITE);
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
        btnBack.setBounds(318, 500, 150, 45);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            soundManager.playEffect("sfx_click.wav");
            backAction.actionPerformed(e);
        });
        add(btnBack);
    }

    /**
     * Loads required external assets including background graphics and custom fonts.
     */
    private void loadResources() {
        try {
            backgroundImage = new ImageIcon("res/assets/images/ui/settings_bg.png").getImage();
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/assets/fonts/icy_font.ttf")).deriveFont(18f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (Exception e) {
            System.err.println("Error loading settings resources: " + e.getMessage());
            customFont = new Font("Arial", Font.BOLD, 18);
        }
    }

    /**
     * Applies custom styling to ComboBox components to ensure visual 
     * consistency with the game's aesthetic.
     */
    private void styleComboBox(JComboBox<String> box) {
        box.setOpaque(false);
        box.setBackground(new Color(0, 0, 0, 0)); 
        box.setBorder(null); 
        box.setFont(customFont.deriveFont(Font.BOLD, 14f));
        box.setForeground(new Color(60, 40, 20)); 

        box.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                // Removes the default arrow button for a cleaner look
                JButton btn = new JButton("");
                btn.setBorder(null);
                btn.setContentAreaFilled(false);
                btn.setOpaque(false);
                btn.setPreferredSize(new Dimension(0, 0));
                return btn;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                // Overridden to maintain transparency for the selected item background
            }

            @Override
            protected ComboPopup createPopup() {
                return new BasicComboPopup(comboBox) {
                    @Override
                    protected JScrollPane createScroller() {
                        JScrollPane scroller = super.createScroller();
                        scroller.setBorder(new LineBorder(new Color(150, 200, 255), 1));
                        return scroller;
                    }
                };
            }
        });

        box.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(customFont.deriveFont(14f));
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                if (index == -1) {
                    label.setOpaque(false); 
                    label.setForeground(new Color(60, 40, 20));
                } else {
                    label.setOpaque(true);
                    if (isSelected) {
                        label.setBackground(new Color(180, 220, 255)); 
                        label.setForeground(Color.BLACK);
                    } else {
                        label.setBackground(new Color(245, 250, 255)); 
                        label.setForeground(new Color(60, 40, 20));
                    }
                }
                return label;
            }
        });
    }

    private void addLabel(String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setFont(customFont);
        label.setForeground(new Color(60, 40, 20));
        label.setBounds(x, y, 200, 30);
        add(label);
    }

    /**
     * Creates a transparent toggle button designed to blend with the background graphics.
     */
    private JButton createToggleButton(boolean state, int x, int y) {
        JButton btn = new JButton(state ? "ON" : "OFF");
        btn.setFont(customFont.deriveFont(Font.BOLD, 16f));
        btn.setBounds(x, y, 160, 30);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setBorder(null); 
        btn.setForeground(new Color(60, 40, 20));
        return btn;
    }

    /**
     * Initializes a button for capturing custom key inputs.
     * Triggers a listening state to reassign keybindings for specific skills.
     */
    private JButton createKeyButton(int initialKey, int skillNum, int x, int y) {
        JButton btn = new JButton(KeyEvent.getKeyText(initialKey));
        btn.setFont(customFont.deriveFont(Font.BOLD, 14f));
        btn.setBounds(x, y, 160, 30);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setForeground(new Color(60, 40, 20));
        btn.setContentAreaFilled(false);
        btn.setBorder(null);

        btn.addActionListener(e -> {
            soundManager.playEffect("sfx_click.wav");
            btn.setText("Press Any Key");
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
                SettingsPanel.this.requestFocusInWindow();
                onChange.run();
            }
        });
        return btn;
    }

    public String getSelectedDifficulty() { return (String) diffBox.getSelectedItem(); }
    public String getSelectedMode() { return (String) modeBox.getSelectedItem(); }
    public boolean isMusicEnabled() { return musicOn; }
    public boolean isSfxEnabled() { return sfxOn; }
    public boolean isMouseEnabled() { return mouseOn; }
    public int getS1Key() { return tempKeyS1; }
    public int getS2Key() { return tempKeyS2; }
    public int getS3Key() { return tempKeyS3; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}