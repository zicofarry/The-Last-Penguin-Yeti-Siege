package com.lastpenguin.view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import com.lastpenguin.model.SQLiteManager;

public class MenuPanel extends JPanel {

    private Image backgroundImage;
    private JTextField nameInputField;
    private JTable leaderboardTable;
    private JScrollPane scrollPane;
    private DefaultTableModel tableModel;
    private JButton playButton, settingsButton, exitButton;
    private Font customFont;
    private Sound SoundManager = new Sound();

    private ActionListener playListener;
    private ActionListener settingsListener;

    // Warna tombol cokelat gelap agar kontras dengan papan kayu
    private final Color buttonNormalColor = new Color(80, 50, 30); 
    private final Color buttonHoverColor = new Color(255, 255, 255); 
    
    // Variabel untuk melacak baris yang sedang di-hover
    private int hoveredRow = -1;

    public MenuPanel(ActionListener playListener, ActionListener settingsListener) {
        this.playListener = playListener;
        this.settingsListener = settingsListener;
        this.setLayout(null);

        loadResources();
        initComponents();
    }

    private void loadResources() {
        try {
            backgroundImage = new javax.swing.ImageIcon("res/assets/images/ui/main_menu_bg.png").getImage();
        } catch (Exception e) {
            System.err.println("Gagal load background menu: " + e.getMessage());
        }

        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/assets/fonts/icy_font.ttf")).deriveFont(24f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (IOException | FontFormatException e) {
            customFont = new Font("Arial", Font.BOLD, 20);
        }
    }

    private void initComponents() {
        Color woodTextColor = new Color(60, 40, 20);

        // --- A. INPUT NAMA (Default Kosong) ---
        nameInputField = new JTextField(""); 
        nameInputField.setBounds(240, 160, 300, 35);
        nameInputField.setOpaque(false);
        nameInputField.setBorder(null);
        if (customFont != null) nameInputField.setFont(customFont.deriveFont(Font.BOLD, 28f));
        nameInputField.setForeground(woodTextColor);
        nameInputField.setHorizontalAlignment(JTextField.CENTER);
        nameInputField.setCaretColor(woodTextColor); 

        nameInputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { SoundManager.playEffect("sfx_keyboard.wav"); }
        });
        this.add(nameInputField);

        // --- B. TABEL LEADERBOARD ---
        String[] columnNames = {"USERNAME", "SCORE", "MISS", "BULLET"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        leaderboardTable = new JTable(tableModel);
        leaderboardTable.setOpaque(false);
        leaderboardTable.setShowGrid(true); 
        leaderboardTable.setGridColor(new Color(60, 40, 20, 100)); 
        leaderboardTable.setIntercellSpacing(new Dimension(1, 1)); 
        leaderboardTable.setRowHeight(30);
        leaderboardTable.setForeground(woodTextColor);
        if (customFont != null) leaderboardTable.setFont(customFont.deriveFont(16f));

        // --- LOGIKA HOVER & KLIK TABEL ---
        leaderboardTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = leaderboardTable.getSelectedRow();
                if (row != -1) {
                    String selectedName = leaderboardTable.getValueAt(row, 0).toString();
                    nameInputField.setText(selectedName);
                    SoundManager.playEffect("sfx_click.wav"); 
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                leaderboardTable.repaint();
            }
        });

        leaderboardTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = leaderboardTable.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    leaderboardTable.repaint();
                }
            }
        });

        // Custom Renderer untuk Efek Hover (Highlight)
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                ((JLabel)c).setHorizontalAlignment(JLabel.CENTER);
                
                // Jika baris sedang di-hover, berikan warna highlight putih transparan
                if (row == hoveredRow) {
                    c.setBackground(new Color(255, 255, 255, 50)); 
                } else {
                    c.setBackground(new Color(0, 0, 0, 0)); 
                }

                if (c instanceof JComponent) {
                    ((JComponent) c).setBorder(null);
                }
                return c;
            }
        };
        
        for (int i = 0; i < leaderboardTable.getColumnCount(); i++) {
            leaderboardTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        leaderboardTable.getTableHeader().setOpaque(false);
        leaderboardTable.getTableHeader().setBackground(new Color(0,0,0,0));
        leaderboardTable.getTableHeader().setForeground(woodTextColor);
        if (customFont != null) leaderboardTable.getTableHeader().setFont(customFont.deriveFont(Font.BOLD, 16f));
        leaderboardTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, woodTextColor));

        scrollPane = new JScrollPane(leaderboardTable);
        scrollPane.setBounds(191, 230, 403, 201); 
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        this.add(scrollPane);

        // --- C. TOMBOL (Play, Settings, Exit) ---
        playButton = createStyledButton("PLAY");
        settingsButton = createStyledButton("SETTINGS");
        exitButton = createStyledButton("EXIT");

        playButton.setBounds(190, 480, 150, 50);
        settingsButton.setBounds(330, 480, 150, 50);
        exitButton.setBounds(475, 480, 150, 50);

        // Menambahkan Sound Effect saat tombol diklik
        playButton.addActionListener(e -> {
            SoundManager.playEffect("sfx_click.wav");
            playListener.actionPerformed(e);
        });

        settingsButton.addActionListener(e -> {
            SoundManager.playEffect("sfx_click.wav");
            settingsListener.actionPerformed(e);
        });

        exitButton.addActionListener(e -> {
            SoundManager.playEffect("sfx_click.wav");
            // Beri sedikit jeda agar suara terdengar sebelum exit
            Timer timer = new Timer(200, ex -> System.exit(0));
            timer.setRepeats(false);
            timer.start();
        });

        this.add(playButton); 
        this.add(settingsButton); 
        this.add(exitButton);
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        if (customFont != null) btn.setFont(customFont.deriveFont(Font.BOLD, 22f));
        btn.setForeground(buttonNormalColor); 
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setForeground(buttonHoverColor); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setForeground(buttonNormalColor); }
        });
        return btn;
    }

    public String getUsername() { return nameInputField.getText(); }

    public void refreshLeaderboard(String difficulty) {
        tableModel.setRowCount(0);
        List<Object[]> dataList = SQLiteManager.getLeaderboardData(difficulty);
        
        for (Object[] row : dataList) {
            tableModel.addRow(new Object[]{
                row[0], row[1], row[2], row[3] 
            });
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}