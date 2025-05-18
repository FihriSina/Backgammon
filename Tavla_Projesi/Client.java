package Tavla_Projesi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Client extends JFrame {

    private JTextField userInputField;
    private JTextArea chatArea;
    private JButton sendButton;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

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
                        chatArea.append("Server: " + messageFromServer + "\n"); // Server'dan gelen mesajı göster
                    }
                } catch (IOException e) {
                    chatArea.append("Server bağlantısı Gitti.\n");
                }
            }).start(); // Thread başlat

        }catch (IOException ex) {
            chatArea.append("Server'a bağlanamadı: " + ex.getMessage() + "\n");
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
