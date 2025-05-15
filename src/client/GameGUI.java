package client;

import javax.swing.*;
import java.awt.*;

public class GameGUI {
    private JFrame frame;
    private JTextArea messageArea;
    private JTextField messageInput;
    private JButton sendButton;

    public GameGUI() {
        frame = new JFrame("Tavla Oyuncu");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        messageArea = new JTextArea();
        messageArea.setEditable(false);

        messageInput = new JTextField(30);
        sendButton = new JButton("Gönder");

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(messageInput);
        panel.add(sendButton);

        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    // Mesajları ekranda göster
    public void showMessage(String msg) {
        messageArea.append(msg + "\n");
    }

    // Send butonuna tıklama için dinleyici ekleme
    public void addSendListener(Runnable listener) {
        sendButton.addActionListener(e -> listener.run());
    }

    // Kullanıcının yazdığı mesajı alma
    public String getMessageInput() {
        return messageInput.getText();
    }

    // Mesaj kutusunu temizleme
    public void clearMessageInput() {
        messageInput.setText("");
    }
}
