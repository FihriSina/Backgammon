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

    private int[] bar = new int[3]; // bar[1]: oyuncu 1, bar[2]: oyuncu 2

    private JLabel barLabel;

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

        JButton resetButton = new JButton("Reset");
        dicePanel.add(resetButton);

        rollDiceButton.setEnabled(false); // Sırası gelmeyene pasif

        dicePanel.add(diceLabel1);
        dicePanel.add(diceLabel2);
        dicePanel.add(rollDiceButton);

        add(dicePanel, BorderLayout.NORTH);

        barLabel = new JLabel("Bar: -"); // ilk değer
        add(barLabel, BorderLayout.WEST);

        // Reset butonu işlevi
        resetButton.addActionListener(e -> {
            output.println("reset_game");
        });

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
                        
                        if (messageFromServer.startsWith("Sunucuya bağlandınız. Oyuncu ID'niz:")) {
                            playerId = Integer.parseInt(messageFromServer.replaceAll("[^0-9]", ""));
                            continue;
                        }

                        if (messageFromServer.equals("SIRA")) {
                            myTurn = true;
                            rollDiceButton.setEnabled(true);
                            chatArea.append("Sıra sizde! Zar atabilirsiniz.\n");
                            continue;
                        }

                        if (messageFromServer.startsWith("OYUN_BITTI:")) {
                            chatArea.append("🎉 " + messageFromServer + "\n");
                            rollDiceButton.setEnabled(false);
                            myTurn = false;
                            return;
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

                        if (messageFromServer.equals("RESET")) {
                            diceLabel1.setText("Zar 1: -");
                            diceLabel2.setText("Zar 2: -");
                            zar1 = -1;
                            zar2 = -1;
                            selectedPoint = -1;
                            chatArea.append("🔁 Oyun sıfırlandı. Zarlar temizlendi.\n");
                            continue;
                        }                   

                        //  Diğer mesajlar (örnek: rakip zar)
                        if (messageFromServer.startsWith("RAKIP_ZAR:")) {
                            String[] parts = messageFromServer.substring(10).split(",");
                            int d1 = Integer.parseInt(parts[0]);
                            int d2 = Integer.parseInt(parts[1]);
                            diceLabel1.setText("Rakip Zar 1: " + d1);
                            diceLabel2.setText("Rakip Zar 2: " + d2);
                            myTurn = false;
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

    /*     // TEST AMAÇLI TAŞ EKLEME
        boardButtons[0].setText("●●●");        // Oyuncu 1 taşları
        boardButtons[23].setText("●●●●●");     // Oyuncu 2 taşları */

    }

/*      TEST AMAÇLI TAHTA GÜNCELLEME
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
    }   */

        private void updateBoardFromString(String data) {
            if (playerId == -1) return; // Oyuncu ID henüz belirlenmediyse çık
        
            String[] points = data.split(";");
            // Bar verisi varsa ayır
            String barInfo = "";
            if (data.contains("|BAR:")) {
                String[] split = data.split("\\|BAR:");
                data = split[0];
                barInfo = split[1];
                points = data.split(";"); // yeniden güncelle!
            }
        
            for (int i = 0; i < 24; i++) {
                String[] parts = points[i].split(",");
                int count = Integer.parseInt(parts[0]);
                int owner = Integer.parseInt(parts[1]);
            
                if (count == 0) {
                    boardButtons[i].setText("Nokta " + i);
                } else {
                    String stones = "";
                    String ownSymbol = (playerId == 1) ? "●" : "○";
                    String opponentSymbol = (playerId == 1) ? "○" : "●";
                
                    for (int j = 0; j < count; j++) {
                        stones += (owner == playerId) ? ownSymbol : opponentSymbol;
                    }
                
                    boardButtons[i].setText(stones);
                }
            }
        
            //  Bar verisi varsa göster
            if (!barInfo.isEmpty()) {

                String ownSymbol = (playerId == 1) ? "●" : "○";
                String[] barParts = barInfo.split(",");
                bar[1] = Integer.parseInt(barParts[0]);
                bar[2] = Integer.parseInt(barParts[1]);
                int ownBarCount = bar[playerId];
                StringBuilder barStones = new StringBuilder("Bar: ");
                for (int i = 0; i < ownBarCount; i++) {
                    barStones.append(ownSymbol);
                }
                barLabel.setText(barStones.toString());
                


            
                if (bar[playerId] > 0) {
                    chatArea.append("Bar'da taşınız var. Önce onu tahtaya çıkarmalısınız.\n");
                }
            }
        }

        private void handlePointClick(int pointIndex) {
            if (!myTurn) {
                chatArea.append("Sıra sizde değil.\n");
                return;
            }
        
            // Eğer bar'da taş varsa sadece bar'dan çıkmaya izin ver
            if (bar[playerId] > 0) {
                int hedef1 = (playerId == 1) ? zar1 - 1 : 24 - zar1;
                int hedef2 = (playerId == 1) ? zar2 - 1 : 24 - zar2;
            
                if (pointIndex != hedef1 && pointIndex != hedef2) {
                    chatArea.append("Bar'daki taşı sadece " + hedef1 + " veya " + hedef2 + ". noktaya koyabilirsiniz.\n");
                    return;
                }
            
                // Bar’dan çıkış hamlesi gönder
                output.println("move:-1->" + pointIndex);
                chatArea.append("Bar'dan çıkış hamlesi gönderildi: -> Nokta " + pointIndex + "\n");
                return;
            }
        
            // Normal taş seçimi
            String ownSymbol = (playerId == 1) ? "●" : "○";
            String btnText = boardButtons[pointIndex].getText();
        
            if (selectedPoint == -1) {
                if (!btnText.startsWith(ownSymbol)) {
                    chatArea.append("Bu noktada kendi taşınız yok!\n");
                    return;
                }
            
                selectedPoint = pointIndex;
                chatArea.append("Taş seçildi: Nokta " + pointIndex + "\n");
                } else {
                    int hedef1 = (playerId == 1) ? selectedPoint + zar1 : selectedPoint - zar1;
                    int hedef2 = (playerId == 1) ? selectedPoint + zar2 : selectedPoint - zar2;
                
                    // 🔁 DIŞARI ÇIKMA KONTROLÜ
                    if ((playerId == 1 && (selectedPoint + zar1 == 24 || selectedPoint + zar2 == 24)) ||
                        (playerId == 2 && (selectedPoint - zar1 == -1 || selectedPoint - zar2 == -1))) {
                        
                        output.println("move:" + selectedPoint + "->OUT");
                        chatArea.append("Taşı dışarı çıkardınız!\n");
                        selectedPoint = -1;
                        return;
                    }
                
                    if (pointIndex != hedef1 && pointIndex != hedef2) {
                        chatArea.append("Zar değerine uygun hamle yapmalısınız.\n");
                        selectedPoint = -1;
                        return;
                    }
                
                    output.println("move:" + selectedPoint + "->" + pointIndex);
                    chatArea.append("Hamle gönderildi: " + selectedPoint + " -> " + pointIndex + "\n");
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
