package tavla;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private static final int PORT = 12345;
    private static ArrayList<SClient> clients = new ArrayList<>();
    private static GameLogic game = new GameLogic();

    public static void main(String[] args) {
        System.out.println("Tavla Server başlatılıyor...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                SClient client = new SClient(socket, clients.size(), game, clients);
                clients.add(client);
                client.start();
            }
        } catch (IOException e) {
            System.err.println("Sunucu hatası: " + e.getMessage());
        }
    }
}
