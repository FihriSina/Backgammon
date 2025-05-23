// Güncellenmiş Client.java
package Tavla_Projesi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Client extends JFrame {

    private JPanel[] boardPanels = new JPanel[24];
    private int selectedPoint = -1;

    private JTextField userInputField;
    private JTextArea chatArea;
    private JButton sendButton;

    private JLabel diceLabel1, diceLabel2;
    private JButton rollDiceButton;
    private JPanel boardPanel;
    private int playerId = -1;
    private boolean myTurn = false;

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    private int zar1 = -1;
    private int zar2 = -1;

    private int[] bar = new int[3];
    private JLabel barLabel;

    private ImageIcon blackStoneIcon;
    private ImageIcon whiteStoneIcon;

    private JButton barSelectButton;
    private boolean barSelected = false;

    private ImageIcon loadIcon(String path) {
        try {
            Image img = new ImageIcon(getClass().getResource(path)).getImage();
            Image scaled = img.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            System.err.println("İkon yüklenemedi: " + path);
            return null;
        }
    }

    private JPanel createPointPanel(int index) {
        JPanel pointPanel = new JPanel();
        pointPanel.setBorder(BorderFactory.createLineBorder(Color.RED));
        pointPanel.setLayout(new BoxLayout(pointPanel, BoxLayout.Y_AXIS));
        pointPanel.setOpaque(false);
        pointPanel.setPreferredSize(new Dimension(50, 120));
        pointPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                handlePointClick(index);
            }
        });
        return pointPanel;
    }

    public Client(String serverIP, int serverPort) {
        setTitle("Tavla Client");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        blackStoneIcon = loadIcon("/res/black_stone.png");
        whiteStoneIcon = loadIcon("/res/white_stone.png");

        JPanel bottomPanel = new JPanel(new BorderLayout());
        userInputField = new JTextField();
        sendButton = new JButton("Gönder");
        barSelectButton = new JButton("Bar'dan Taş Seç");
        barSelectButton.setEnabled(false);
        barSelectButton.addActionListener(e -> {
            if (bar[playerId] > 0 && myTurn) {
                barSelected = true;
                chatArea.append("Bar'daki taşı çıkarmak için hedef noktaya tıklayın.\n");
            }
        });

        bottomPanel.add(userInputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        bottomPanel.add(barSelectButton, BorderLayout.WEST);
        add(bottomPanel, BorderLayout.SOUTH);

        JPanel dicePanel = new JPanel();
        diceLabel1 = new JLabel("Zar 1: -");
        diceLabel2 = new JLabel("Zar 2: -");
        rollDiceButton = new JButton("Zar At");
        JButton resetButton = new JButton("Reset");
        dicePanel.add(resetButton);
        dicePanel.add(diceLabel1);
        dicePanel.add(diceLabel2);
        dicePanel.add(rollDiceButton);
        rollDiceButton.setEnabled(false);
        add(dicePanel, BorderLayout.NORTH);

        barLabel = new JLabel("Bar: -");
        barLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        barLabel.setForeground(Color.RED);
        barLabel.setBorder(BorderFactory.createTitledBorder("Bar Alanı"));
        add(barLabel, BorderLayout.WEST);

        Image bg = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/res/tavlatahtasi.png"));
        boardPanel = new JPanel(new GridLayout(2, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            }
        };

        for (int i = 23; i >= 12; i--) {
            JPanel pointPanel = createPointPanel(i);
            boardPanels[i] = pointPanel;
            boardPanel.add(pointPanel);
        }
        for (int i = 0; i <= 11; i++) {
            JPanel pointPanel = createPointPanel(i);
            boardPanels[i] = pointPanel;
            boardPanel.add(pointPanel);
        }

        add(boardPanel, BorderLayout.EAST);

        resetButton.addActionListener(e -> output.println("reset_game"));
        rollDiceButton.addActionListener(e -> {
            if (myTurn) output.println("roll_dice");
        });

        sendButton.addActionListener(e -> sendMessage());
        userInputField.addActionListener(e -> sendMessage());

        try {
            socket = new Socket(serverIP, serverPort);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            chatArea.append("Server'a bağlandı: " + serverIP + "\n");

            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = input.readLine()) != null) {
                        if (msg.startsWith("Sunucuya bağlandınız.")) {
                            playerId = Integer.parseInt(msg.replaceAll("[^0-9]", ""));
                            chatArea.append("Oyuncu ID: " + playerId + "\n");
                            continue;
                        }
                        if (msg.equals("SIRA")) {
                            myTurn = true;
                            rollDiceButton.setEnabled(true);
                            chatArea.append("Sıra sizde! Zar atabilirsiniz.\n");
                            continue;
                        }
                        if (msg.startsWith("OYUN_BITTI:")) {
                            chatArea.append("🎉 " + msg + "\n");
                            rollDiceButton.setEnabled(false);
                            myTurn = false;
                            continue; 
                        }
                        if (msg.startsWith("TAHTA:")) {
                            updateBoardFromString(msg.substring(6));
                            continue;
                        }
                        if (msg.startsWith("ZARLAR:")) {
                            String[] z = msg.substring(7).split(",");
                            zar1 = Integer.parseInt(z[0]);
                            zar2 = Integer.parseInt(z[1]);
                            diceLabel1.setText("Zar 1: " + zar1);
                            diceLabel2.setText("Zar 2: " + zar2);
                            rollDiceButton.setEnabled(false);
                            chatArea.append("Zarlarınız geldi: " + zar1 + " ve " + zar2 + "\n");
                            continue;
                        }
                        if (msg.equals("RESET")) {
                            diceLabel1.setText("Zar 1: -");
                            diceLabel2.setText("Zar 2: -");
                            zar1 = zar2 = -1;
                            selectedPoint = -1;
                            chatArea.append("🔁 Oyun sıfırlandı. Zarlar temizlendi.\n");
                            continue;
                        }
                        if (msg.startsWith("RAKIP_ZAR:")) {
                            String[] r = msg.substring(10).split(",");
                            diceLabel1.setText("Rakip Zar 1: " + r[0]);
                            diceLabel2.setText("Rakip Zar 2: " + r[1]);
                            myTurn = false;
                            rollDiceButton.setEnabled(false);
                            chatArea.append("Rakip zar attı: " + r[0] + " ve " + r[1] + "\n");
                            continue;
                        }
                        chatArea.append("Server: " + msg + "\n");
                    }
                } catch (IOException ex) {
                    chatArea.append("Bağlantı kesildi.\n");
                }
            }).start();
        } catch (IOException e) {
            chatArea.append("Server bağlantı hatası: " + e.getMessage() + "\n");
        }
    }

    private void updateBoardFromString(String data) {
        if (playerId == -1) return;
        String[] points = data.split(";");
        String barInfo = "";
        if (data.contains("|BAR:")) {
            String[] split = data.split("\\|BAR:");
            data = split[0];
            barInfo = split[1];
            points = data.split(";");
        }

        for (int i = 0; i < 24; i++) {
            String[] p = points[i].split(",");
            int count = Integer.parseInt(p[0]);
            int owner = Integer.parseInt(p[1]);
            boardPanels[i].removeAll();

            for (int j = 0; j < count; j++) {
                JLabel lbl = new JLabel();
                lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
                lbl.setPreferredSize(new Dimension(30, 30));
                ImageIcon baseIcon = (owner == 1) ? blackStoneIcon : whiteStoneIcon;
                if (baseIcon != null) {
                    Image scaledImg = baseIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                    lbl.setIcon(new ImageIcon(scaledImg));
                }
                lbl.putClientProperty("owner", owner);
                boardPanels[i].add(lbl);
            }
            boardPanels[i].revalidate();
            boardPanels[i].repaint();
        }

        if (!barInfo.isEmpty()) {
            String ownSymbol = (playerId == 1) ? "⚫" : "⚪";
            String[] barParts = barInfo.split(",");
            bar[1] = Integer.parseInt(barParts[0]);
            bar[2] = Integer.parseInt(barParts[1]);
            StringBuilder barStr = new StringBuilder("Bar: ");
            for (int i = 0; i < bar[playerId]; i++) barStr.append(ownSymbol);
            barLabel.setText(barStr.toString());
            barSelectButton.setEnabled(bar[playerId] > 0 && myTurn);
            if (bar[playerId] > 0)
                chatArea.append("Bar'da taşınız var. Önce onu tahtaya koymalısınız.\n");
        }
    }

    private void handlePointClick(int pointIndex) {
        if (!myTurn) {
            chatArea.append("Sıra sizde değil.\n");
            return;
        }

        if (bar[playerId] > 0 && !barSelected) {
            chatArea.append("Bar'daki taşı çıkarmalısınız. Önce Bar butonuna tıklayın.\n");
            return;
        }

        if (barSelected) {
            int h1 = (playerId == 1) ? zar1 - 1 : 24 - zar1;
            int h2 = (playerId == 1) ? zar2 - 1 : 24 - zar2;

            if (pointIndex != h1 && pointIndex != h2) {
                chatArea.append("Bar'daki taşı sadece " + h1 + " veya " + h2 + ". noktaya koyabilirsiniz.\n");
                return;
            }

            output.println("move:-1->" + pointIndex);
            chatArea.append("Bar'dan hamle: -> " + pointIndex + "\n");
            barSelected = false;
            return;
        }

        int count = boardPanels[pointIndex].getComponentCount();
        if (selectedPoint == -1) {
            if (count == 0) {
                chatArea.append("Taş yok.\n");
                return;
            }
            JLabel topStone = (JLabel) boardPanels[pointIndex].getComponent(count - 1);
            Integer owner = (Integer) topStone.getClientProperty("owner");
            if (owner == null || owner != playerId) {
                chatArea.append("Bu taş size ait değil.\n");
                return;
            }
            selectedPoint = pointIndex;
            boardPanels[selectedPoint].setBackground(Color.YELLOW); // Vurgulu seçim
            chatArea.append("Taş seçildi: " + pointIndex + "\n");              
        } else {
            int h1 = (playerId == 1) ? selectedPoint + zar1 : selectedPoint - zar1;
            int h2 = (playerId == 1) ? selectedPoint + zar2 : selectedPoint - zar2;

            if ((playerId == 1 && (selectedPoint + zar1 == 24 || selectedPoint + zar2 == 24)) ||
                (playerId == 2 && (selectedPoint - zar1 == -1 || selectedPoint - zar2 == -1))) {
                output.println("move:" + selectedPoint + "->OUT");
                chatArea.append("Taş dışarı çıkarıldı.\n");
                selectedPoint = -1;
                return;
            }

            if (pointIndex != h1 && pointIndex != h2) {
                chatArea.append("Zara uygun nokta değil.\n");
                selectedPoint = -1;
                return;
            }

            output.println("move:" + selectedPoint + "->" + pointIndex);
            chatArea.append("Hamle: " + selectedPoint + " -> " + pointIndex + "\n");
            boardPanels[selectedPoint].setBackground(null); // Arka planı temizle
            selectedPoint = -1;
        }
    }

    private void sendMessage() {
        String msg = userInputField.getText().trim();
        if (!msg.isEmpty()) {
            output.println(msg);
            chatArea.append("Sen: " + msg + "\n");
            userInputField.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Client client = new Client("127.0.0.1", 5000);
            client.setVisible(true);
        });
    }
}
