package Tavla_Projesi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Client extends JFrame {

    private JButton[] boardButtons = new JButton[24]; // 24 nokta
    private int selectedPoint = -1; // Seçilen taşın konumu

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


    public Client(String serverIP, int serverPort) {
        
        //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // GUI KISMI
        setTitle("Tavla Client");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Pence kapanınca uygulama kapansın
        setLayout(new BorderLayout()); // Pencere içi düzeni

        //Sohbet Alanı
        chatArea = new JTextArea();
        chatArea.setEditable(false); // Kullanıcı düzenleyemez
        chatArea.setLineWrap(true); // Kullanıcının yazdığı metnin satır sonuna gelindiğinde otomatik olarak alt satıra geçmesini sağlar. 
        chatArea.setWrapStyleWord(true); // Kelime bazlı satır kaydırma işlemi yaparak kelimelerin bölünmeden düzenli şekilde alt satıra geçmesini sağlar.
        add(new JScrollPane(chatArea), BorderLayout.CENTER); // Kaydırılabilir alan

        // Kullanıcı Giriş Alanı - Gönderme Butonu
        JPanel buttomPanel = new JPanel(new BorderLayout());
        userInputField = new JTextField();
        sendButton = new JButton("Gönder");

        buttomPanel.add(userInputField, BorderLayout.CENTER);   
        buttomPanel.add(sendButton, BorderLayout.EAST); // Buton sağa yaslı

        add(buttomPanel, BorderLayout.SOUTH); // Buton panelini alt kısma ekle

        //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        
        // Zar bilgisi paneli
        JPanel dicePanel = new JPanel();
        diceLabel1 = new JLabel("Zar 1: -");
        diceLabel2 = new JLabel("Zar 2: -");
        rollDiceButton = new JButton("Zar At");
        rollDiceButton.setEnabled(false); // Sırası gelmeyene pasif

        dicePanel.add(diceLabel1);
        dicePanel.add(diceLabel2);
        dicePanel.add(rollDiceButton);

        add(dicePanel, BorderLayout.NORTH);

        // Zar At butonu işlevi
        rollDiceButton.addActionListener(e -> {
            if (myTurn) {
                output.println("roll_dice"); // Server’a zar at komutu gönder
            }
        });

        // Butona basıldığında mesaj gönderme
        sendButton.addActionListener(e -> sendMessage());     // Butona tıklandığında mesaj gönderme
        userInputField.addActionListener(e -> sendMessage()); // Enter tuşuna basıldığında mesaj gönderme

        // Server bağlantısı
        try {
            socket = new Socket(serverIP, serverPort);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            chatArea.append("Server'a bağlandı: " + serverIP + "\n"); // Server'a bağlandığında mesaj göster

            // Sunucudan gelen mesahları okumak için thread

            new Thread(() -> {
                try {
                    String messageFromServer;
                    while ((messageFromServer = input.readLine()) != null) {
                        
                        if (messageFromServer.equals("SIRA")) {
                            myTurn = true;
                            rollDiceButton.setEnabled(true);
                            chatArea.append("Sıra sizde! Zar atabilirsiniz.\n");
                            continue;
                        }

                        if (messageFromServer.startsWith("TAHTA:")) {
                            String boardData = messageFromServer.substring(6);
                            updateBoardFromString(boardData);
                            continue;
                        }

                        if (messageFromServer.startsWith("TAHTA:")) {
                            String boardData = messageFromServer.substring(6);
                            updateBoardFromString(boardData);
                            continue;
                        }

                        if (messageFromServer.startsWith("ZARLAR:")) {
                            // Örn: ZARLAR:3,5
                            String[] parts = messageFromServer.substring(7).split(",");
                            int d1 = Integer.parseInt(parts[0]);
                            int d2 = Integer.parseInt(parts[1]);
                            diceLabel1.setText("Zar 1: " + d1);
                            diceLabel2.setText("Zar 2: " + d2);
                            zar1 = d1;
                            zar2 = d2;

                            myTurn = true; // kendi sırası geldi
                            rollDiceButton.setEnabled(false); // zar zaten atıldı
                            chatArea.append("Zarlarınız geldi: " + d1 + " ve " + d2 + "\n");
                            continue;
                        }

                        if (messageFromServer.startsWith("RAKIP_ZAR:")) {
                            String[] parts = messageFromServer.substring(10).split(",");
                            int d1 = Integer.parseInt(parts[0]);
                            int d2 = Integer.parseInt(parts[1]);
                            diceLabel1.setText("Rakip Zar 1: " + d1);
                            diceLabel2.setText("Rakip Zar 2: " + d2);
                            myTurn = false; // sırası bizde değil
                            rollDiceButton.setEnabled(false);
                            chatArea.append("Rakip zar attı: " + d1 + " ve " + d2 + "\n");
                            continue;
                        }

                        // Diğer normal mesajlar
                        chatArea.append("Server: " + messageFromServer + "\n");
                    }
                } catch (IOException e) {
                    chatArea.append("Server bağlantısı gitti.\n");
                }
            }).start();


        }catch (IOException ex) {
            chatArea.append("Server'a bağlanamadı: " + ex.getMessage() + "\n");
        }
        
        // TAHTA PANELİ
        boardPanel = new JPanel(new GridLayout(2, 12)); // 2 satır 12 sütun = 24 nokta
        for (int i = 0; i < 24; i++) {
            JButton pointButton = new JButton("Nokta " + i);
            int pointIndex = i;
    
            pointButton.addActionListener(e -> handlePointClick(pointIndex));
            boardButtons[i] = pointButton;
            boardPanel.add(pointButton);
        }

        add(boardPanel, BorderLayout.EAST); // Sağa yerleştirdik

        // TEST AMAÇLI TAŞ EKLEME
        boardButtons[0].setText("●●●");        // Oyuncu 1 taşları
        boardButtons[23].setText("●●●●●");     // Oyuncu 2 taşları

    }

    private void updateBoard(int[][] board) {
        for (int i = 0; i < 24; i++) {
            int count = board[i][0];
            int owner = board[i][1];

            if (count == 0) {
                boardButtons[i].setText("Nokta " + i);
            } else {
                String stones = "";
                for (int j = 0; j < count; j++) {
                    stones += (owner == playerId) ? "●" : "○";
                }
                boardButtons[i].setText(stones);
            }
        }
    }   

    private void updateBoardFromString(String data) {
        String[] points = data.split(";");
        for (int i = 0; i < 24; i++) {
            String[] parts = points[i].split(",");
            int count = Integer.parseInt(parts[0]);
            int owner = Integer.parseInt(parts[1]);

            if (count == 0) {
                boardButtons[i].setText("Nokta " + i);
            } else {
                String stones = "";
                for (int j = 0; j < count; j++) {
                    stones += (owner == playerId) ? "●" : "○";
                }
                boardButtons[i].setText(stones);
            }
        }
    }



    private void handlePointClick(int pointIndex) {
        if (!myTurn) {
            chatArea.append("Sıra sizde değil.\n");
            return;
        }

        if (selectedPoint == -1) {
            selectedPoint = pointIndex;
            chatArea.append("Taş seçildi: Nokta " + pointIndex + "\n");
        } else {
            // Zar değerine göre geçerli mi kontrol et
            int expectedTo1 = selectedPoint + zar1;
            int expectedTo2 = selectedPoint + zar2;

            if (pointIndex != expectedTo1 && pointIndex != expectedTo2) {
                chatArea.append("Zar değerlerine uygun hamle yapmalısınız.\n");
                selectedPoint = -1;
                return;
            }

            chatArea.append("Hamle: " + selectedPoint + " -> " + pointIndex + "\n");
            output.println("move:" + selectedPoint + "->" + pointIndex);

            selectedPoint = -1;
        }
    }



    // Mesaj gönderme fonksiyonu
    private void sendMessage() {
        String message = userInputField.getText();
        if(!message.trim().isEmpty()) { // Mesaj boş değilse
            output.println(message); // Server'a mesaj gönder
            chatArea.append("Sen: " + message + "\n"); // Kendi mesajını göster
            userInputField.setText(""); // Giriş alanını temizle
        }
    }

    public static void main(String[] args) {
        //GUI BAŞLIOR -- AWS IP
        SwingUtilities.invokeLater(() -> {
            Client client = new Client("127.0.0.1", 5000); // SERVER IP BURADA | 104.199.56.156
            client.setVisible(true); // Pencereyi görünür yap
        });
    }
}
