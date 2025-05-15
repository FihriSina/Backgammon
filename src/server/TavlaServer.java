package server;

import java.io.*;
import java.net.*;
import java.util.*;
import protocol.Message;

public class TavlaServer {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients;

    public TavlaServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clients = new ArrayList<>();
        System.out.println("Tavla Server " + port + " portunda başladı.");
    }

    public void start() {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Yeni oyuncu bağlanıyor
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                handler.start();
                System.out.println("Yeni oyuncu bağlandı: " + clientSocket.getInetAddress());
            }
        } catch (IOException e) {
            System.out.println("Sunucu kapandı.");
        }
    }

    // Mesajı diğer oyunculara gönder
    public synchronized void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    // Sunucu programının ana noktası
    public static void main(String[] args) {
        try {
            TavlaServer server = new TavlaServer(5000);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Her oyuncuyu ayrı thread'de yönetecek iç sınıf
    class ClientHandler extends Thread {
        private Socket socket;
        private TavlaServer server;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket, TavlaServer server) throws IOException {
            this.socket = socket;
            this.server = server;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Sunucu aldı: " + message);

                    // Mesajı protokol üzerinden analiz edebiliriz
                    Message.ParsedMessage parsed = Message.parse(message);

                    // Burada oyun mantığına göre mesaj işlenir
                    // Şimdilik sadece diğer oyunculara gönderiyoruz
                    server.broadcast(message, this);
                }
            } catch (IOException e) {
                System.out.println("Bir oyuncu bağlantısını kaybetti.");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                server.clients.remove(this);
            }
        }
    }
}
