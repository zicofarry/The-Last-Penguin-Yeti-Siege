package com.lastpenguin.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class MenuPanel extends JPanel {

    private Image backgroundImage;
    private JTextField nameInputField;
    private JTextArea leaderboardArea;
    private JButton playButton, settingsButton, exitButton;
    private Font customFont;
    private Sound SoundManager = new Sound();

    // PERBAIKAN 1a: Simpan listener yang diterima dari Main
    private ActionListener playListener;
    private ActionListener settingsListener;

    // PERBAIKAN 1b: Constructor diubah untuk menerima listener dari Main.java
    public MenuPanel(ActionListener playListener, ActionListener settingsListener) {
        this.playListener = playListener;
        this.settingsListener = settingsListener;

        // PENTING: Gunakan null layout untuk penempatan pixel presisi di atas gambar
        this.setLayout(null);

        loadResources();
        initComponents();
    }

    private void loadResources() {
        // 1. Load Gambar Background
        try {
            // Pastikan path ini benar
            backgroundImage = new javax.swing.ImageIcon("res/assets/images/ui/main_menu_bg.png").getImage();
        } catch (Exception e) {
            System.err.println("Gagal load background menu: " + e.getMessage());
        }

        // 2. Load Font Kustom
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/assets/fonts/icy_font.ttf")).deriveFont(24f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (IOException | FontFormatException e) {
            System.err.println("Gagal load font, pakai default Arial. Pastikan file .ttf ada di res/assets/fonts/");
            customFont = new Font("Arial", Font.BOLD, 24);
        }
    }

    private void initComponents() {
        // Warna untuk teks agar terlihat seperti di atas kayu (cokelat tua)
        Color woodTextColor = new Color(60, 40, 20);

        // --- A. SETUP INPUT NAMA (Papan Atas) ---
        nameInputField = new JTextField("Player Name");
        // TENTUKAN KOORDINAT DISINI: setBounds(x, y, width, height)
        // NANTI HARUS DISESUAIKAN LAGI AGAR PAS DENGAN GAMBAR
        nameInputField.setBounds(240, 160, 300, 30);

        // Styling
        nameInputField.setOpaque(false);
        nameInputField.setBorder(null);
        if (customFont != null) {
             nameInputField.setFont(customFont.deriveFont(Font.BOLD, 28f));
        }
        nameInputField.setForeground(woodTextColor);
        nameInputField.setHorizontalAlignment(JTextField.CENTER);
        nameInputField.getCaret().setVisible(true);

        // Efek Suara Keyboard
        nameInputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                // PERBAIKAN 3: Baris ini dikomen dulu karena SoundManager belum siap
                SoundManager.playEffect("sfx_keyboard.wav");
                 // System.out.println("Bunyi: Tik! (Placeholder)"); // Debug
            }
        });
        this.add(nameInputField);


        // --- B. SETUP LEADERBOARD (Papan Tengah Besar) ---
        leaderboardArea = new JTextArea();
        // TENTUKAN KOORDINAT DISINI NANTI:
        leaderboardArea.setBounds(150, 150, 500, 300);

        // Styling
        leaderboardArea.setOpaque(false);
        leaderboardArea.setBorder(null);
        if (customFont != null) {
            leaderboardArea.setFont(customFont.deriveFont(20f));
        }
        leaderboardArea.setForeground(woodTextColor);
        leaderboardArea.setEditable(false);
        leaderboardArea.setLineWrap(true);
        leaderboardArea.setWrapStyleWord(true);
        leaderboardArea.setText("TOP SCORES:\n(Loading...)");
        this.add(leaderboardArea);


        // --- C. SETUP TOMBOL (Papan Bawah) ---
        playButton = createStyledButton("PLAY");
        settingsButton = createStyledButton("SETTINGS");
        exitButton = createStyledButton("EXIT");

        // TENTUKAN KOORDINAT TOMBOL NANTI:
        int buttonY = 480;
        int buttonWidth = 150;
        int buttonHeight = 50;

        playButton.setBounds(190, buttonY, buttonWidth, buttonHeight);
        settingsButton.setBounds(330, buttonY, buttonWidth, buttonHeight);
        exitButton.setBounds(475, buttonY, buttonWidth, buttonHeight);

        // PERBAIKAN 1c: Gunakan listener yang dikirim dari Main.java
        playButton.addActionListener(this.playListener);
        settingsButton.addActionListener(this.settingsListener);

        // Tombol exit bisa langsung ditangani di sini
        exitButton.addActionListener(e -> System.exit(0));


        this.add(playButton);
        this.add(settingsButton);
        this.add(exitButton);
    }


    // Helper method untuk membuat tombol
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        if (customFont != null) {
            btn.setFont(customFont.deriveFont(Font.BOLD, 22f));
        }
        btn.setForeground(new Color(220, 200, 180));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setForeground(Color.WHITE); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setForeground(new Color(220, 200, 180)); }
        });
        return btn;
    }

    // PERBAIKAN 2: Ubah nama method jadi getUsername() agar sesuai dengan Main.java
    public String getUsername() {
        return nameInputField.getText();
    }

    // Method untuk update leaderboard dari luar (nanti dipakai Presenter)
    public void updateLeaderboard(String text) {
        leaderboardArea.setText(text);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
        }
    }
}