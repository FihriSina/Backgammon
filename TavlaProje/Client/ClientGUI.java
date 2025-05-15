// ClientGUI.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ClientGUI extends JFrame {
    private JTextField txtUsername, txtMessage;
    private JTextArea chatArea, scoreArea;
    private JButton btnSend, btnYeniOyun;
    private static boolean myTurn = false;
    private PrintWriter out;
    private GamePanel gamePanel;
    private boolean isReadingScores = false;

    public ClientGUI() {
        setTitle("Tavla Client");
        setSize(700, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        txtUsername = new JTextField();
        txtUsername.setBorder(BorderFactory.createTitledBorder("Kullanıcı Adı"));
        add(txtUsername, BorderLayout.NORTH);

        chatArea = new JTextArea();
        chatArea.setEditable(false);

        scoreArea = new JTextArea(5, 20);
        scoreArea.setEditable(false);
        scoreArea.setBorder(BorderFactory.createTitledBorder("Skorlar"));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        txtMessage = new JTextField();
        btnSend = new JButton("Gönder");
        btnYeniOyun = new JButton("Yeni Oyun");
        btnYeniOyun.setEnabled(false);

        bottomPanel.add(btnYeniOyun, BorderLayout.WEST);
        bottomPanel.add(txtMessage, BorderLayout.CENTER);
        bottomPanel.add(btnSend, BorderLayout.EAST);

        gamePanel = new GamePanel(this);
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Sohbet", new JScrollPane(chatArea));
        tabbedPane.addTab("Tavla", gamePanel);

        add(tabbedPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        add(scoreArea, BorderLayout.EAST);

        txtUsername.addActionListener(e -> connect());
        btnSend.addActionListener(e -> sendMessage());
        btnYeniOyun.addActionListener(e -> {
            if (out != null) {
                out.println("YENIOYUN");
            }
        });

        setVisible(true);
    }

    private void connect() {
        try {
            Socket socket = new Socket("127.0.0.1", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(() -> {
                String line;
                try {
                    while ((line = in.readLine()) != null) {
                        if (line.equals("Senin sıran!")) {
                            myTurn = true;
                            chatArea.append("[Oyun] Sıra sende.\n");
                        } else if (line.equals("Rakibini bekliyorsun...")) {
                            myTurn = false;
                            chatArea.append("[Oyun] Bekleme sırası.\n");
                        } else if (line.startsWith("Sıra sende değil")) {
                            chatArea.append("[Uyarı] " + line + "\n");
                        } else if (line.startsWith("HAMLE:")) {
                            String move = line.substring(6);
                            gamePanel.applyMove(move);
                            chatArea.append("[Hamle] " + move + "\n");
                        } else if (line.startsWith("ZAR:")) {
                            String zar = line.substring(4);
                            chatArea.append("[Zar] " + zar + "\n");
                        } else if (line.startsWith("[Oyun Bitti]")) {
                            chatArea.append(line + "\n");
                            JOptionPane.showMessageDialog(this, line);
                            btnYeniOyun.setEnabled(true);
                        } else if (line.equals("RESET")) {
                            chatArea.append("[Sunucu] Yeni oyun başlatıldı!\n");
                            gamePanel.resetBoard();
                            btnYeniOyun.setEnabled(false);
                        } else if (line.equals("[Skor Tablosu]")) {
                            scoreArea.setText("");
                            isReadingScores = true;
                        } else if (line.equals("[Skor Tablosu Son]")) {
                            isReadingScores = false;
                        } else if (isReadingScores && line.startsWith("SCORE:")) {
                            String[] parts = line.split(":");
                            if (parts.length == 3) {
                                scoreArea.append(parts[1] + ": " + parts[2] + "\n");
                            }
                        } else {
                            chatArea.append(line + "\n");
                        }
                    }
                } catch (IOException e) {
                    chatArea.append("Sunucu ile bağlantı kesildi.\n");
                }
            }).start();

            out.println(txtUsername.getText());
            txtUsername.setEditable(false);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Bağlantı hatası: " + e.getMessage());
        }
    }

    private void sendMessage() {
        if (out != null) {
            out.println(txtMessage.getText());
            txtMessage.setText("");
        }
    }

    public void sendMove(String move) {
        if (out != null) {
            out.println("HAMLE:" + move);
        }
    }

    public static boolean isMyTurn() {
        return myTurn;
    }

    public static void setMyTurn(boolean val) {
        myTurn = val;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}
