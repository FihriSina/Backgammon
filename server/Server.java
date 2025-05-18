package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    public static final int PORT = 12345;
    private static final List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Tavla Sunucusu başlatılıyor...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (clients.size() < 2) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Yeni bir oyuncu bağlandı.");

                ClientHandler clientHandler = new ClientHandler(clientSocket, clients.size());
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }

            System.out.println("İki oyuncu bağlandı, oyun başlıyor!");
            clients.get(0).sendMessage("START 1"); // Oyuncu 1 başlasın
            clients.get(1).sendMessage("START 2");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(String message, int senderId) {
        for (ClientHandler client : clients) {
            if (client.getPlayerId() != senderId) {
                client.sendMessage(message);
            }
        }
    }

    public static void resetGame() {
        // Gerekirse GameState sıfırlanabilir burada
    }
}
