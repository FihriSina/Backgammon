package tavla;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class GUIClient extends JFrame {
    private JLabel boardLabel;
    private JLabel dice1Label, dice2Label;
    private JButton rollButton;
    private ObjectOutputStream output;
    private ObjectInputStream input;

    private JButton[] pointButtons = new JButton[24];
    private int selectedFrom = -1;

    public GUIClient() {
        setTitle("Tavla Oyunu - GUI Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLayout(null);

        // Tahta görseli
        boardLabel = new JLabel(new ImageIcon("assets/tavla_board.png"));
        boardLabel.setBounds(0, 0, 900, 500);
        add(boardLabel);

        // Tahta üstü tıklanabilir 24 hane butonu
        for (int i = 0; i < 24; i++) {
            JButton pointButton = new JButton();
            pointButton.setBounds(getXForPoint(i), getYForPoint(i), 30, 30);
            pointButton.setContentAreaFilled(false);
            pointButton.setBorderPainted(true);
            final int pointIndex = i;
            pointButton.addActionListener(e -> handlePointClick(pointIndex));
            pointButtons[i] = pointButton;
            add(pointButton);
        }

        // Zarlar
        dice1Label = new JLabel(new ImageIcon("assets/dice_1.png"));
        dice2Label = new JLabel(new ImageIcon("assets/dice_1.png"));
        dice1Label.setBounds(300, 510, 64, 64);
        dice2Label.setBounds(370, 510, 64, 64);
        add(dice1Label);
        add(dice2Label);

        // Zar At Butonu
        rollButton = new JButton("Zar At");
        rollButton.setBounds(460, 510, 100, 30);
        rollButton.addActionListener(e -> {
            try {
                output.writeObject(new Message(Message.MessageType.ROLL_DICE, 0, null));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Zar gönderilemedi.");
            }
        });
        add(rollButton);

        connectToServer();
    }

    private void handlePointClick(int index) {
        if (selectedFrom == -1) {
            selectedFrom = index;
            JOptionPane.showMessageDialog(this, "Başlangıç noktası seçildi: " + index);
        } else {
            int to = index;
            try {
                output.writeObject(new Message(Message.MessageType.MOVE, 0, new int[]{selectedFrom, to}));
                selectedFrom = -1;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Hamle gönderilemedi.");
            }
        }
    }

    private int getXForPoint(int index) {
        int spacing = 35;
        if (index < 12) return 800 - (index * spacing);
        else return 50 + ((index - 12) * spacing);
    }

    private int getYForPoint(int index) {
        return index < 12 ? 20 : 400;
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("127.0.0.1", 12345);
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());

            Thread listener = new Thread(() -> {
                try {
                    while (true) {
                        Message msg = (Message) input.readObject();
                        switch (msg.getType()) {
                            case ROLL_DICE:
                                int[] dice = (int[]) msg.getData();
                                updateDice(dice);
                                break;
                            case INFO:
                            case CHAT:
                                JOptionPane.showMessageDialog(this, msg.getData().toString());
                                break;
                            case UPDATE:
                                JOptionPane.showMessageDialog(this, "Tahta güncellendi.");
                                break;
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    JOptionPane.showMessageDialog(this, "Bağlantı kesildi.");
                }
            });
            listener.start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Sunucuya bağlanılamadı: " + e.getMessage());
        }
    }

    private void updateDice(int[] dice) {
        dice1Label.setIcon(new ImageIcon("assets/dice_" + dice[0] + ".png"));
        dice2Label.setIcon(new ImageIcon("assets/dice_" + dice[1] + ".png"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUIClient client = new GUIClient();
            client.setVisible(true);
        });
    }
}
