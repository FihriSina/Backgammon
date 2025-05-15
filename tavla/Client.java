package tavla;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        final String SERVER_IP = "127.0.0.1";
        final int SERVER_PORT = 12345;

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Sunucuya bağlanıldı.");

            Thread listener = new Thread(() -> {
                try {
                    while (true) {
                        Message msg = (Message) input.readObject();
                        switch (msg.getType()) {
                            case CONNECT:
                            case INFO:
                            case CHAT:
                                System.out.println("[Bilgi] " + msg.getData());
                                break;
                            case ROLL_DICE:
                                System.out.println("[Zarlar] " + Arrays.toString((int[]) msg.getData()));
                                break;
                            case UPDATE:
                                System.out.println("[Tahta Güncellendi]");
                                break;
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Bağlantı kesildi.");
                }
            });
            listener.start();

            while (true) {
                System.out.println("\nKomut girin (roll, move, bar, bear, chat, end, quit): ");
                String cmd = scanner.nextLine();

                switch (cmd) {
                    case "roll":
                        output.writeObject(new Message(Message.MessageType.ROLL_DICE, 0, null));
                        break;
                    case "move":
                        System.out.print("from to (örnek: 12 17): ");
                        int from = scanner.nextInt();
                        int to = scanner.nextInt();
                        scanner.nextLine();
                        output.writeObject(new Message(Message.MessageType.MOVE, 0, new int[]{from, to}));
                        break;
                    case "bar":
                        System.out.print("zar değeri ile giriş yap (örn: 5): ");
                        int diceVal = scanner.nextInt();
                        scanner.nextLine();
                        output.writeObject(new Message(Message.MessageType.ENTER_BAR, 0, diceVal));
                        break;
                    case "bear":
                        System.out.print("hangi haneden çıkaracaksın?: ");
                        int bearFrom = scanner.nextInt();
                        scanner.nextLine();
                        output.writeObject(new Message(Message.MessageType.TRY_BEAR_OFF, 0, bearFrom));
                        break;
                    case "end":
                        output.writeObject(new Message(Message.MessageType.NEXT_TURN, 0, null));
                        break;
                    case "chat":
                        System.out.print("mesaj: ");
                        String msg = scanner.nextLine();
                        output.writeObject(new Message(Message.MessageType.CHAT, 0, msg));
                        break;
                    case "quit":
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Bilinmeyen komut.");
                }
            }

        } catch (IOException e) {
            System.err.println("Sunucuya bağlanırken hata: " + e.getMessage());
        }
    }
}
