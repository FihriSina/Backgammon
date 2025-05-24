package Tavla_Projesi;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.*;

public class Server {

    private static final int PORT = 5000;
    // Bağlantıları yönetecek thread havuzu oluşturuyorum. (Performan Etkili)
    private static ExecutorService pool = Executors.newFixedThreadPool(10); // 10 threadli havuz

    // Oyuncu bağlantı listesi
    private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    // Oyun nesnesi (tavla mantığı için)
    public static Game game = new Game(); // Game sınıfını aldık.

    public static void main(String[] args){
        
        System.out.println("Server başlatılıyor...");

        try {
            // Sunucu soketini 5000 num' lı porta bağlıyorum.
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server " + PORT + " portunda dinleniyor...");

            while (clients.size() < 2) { // 2 oyuncu bağlantısı bekliyorum

                // İstemci bağlantısı kabul etme
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client Geldi: " + clientSocket.getInetAddress());
                ClientHandler handler = new ClientHandler(clientSocket, clients.size() + 1);
                // Her Client için -> Bir thread
                clients.add(handler);
                pool.execute(handler);
                System.out.println("Yeni oyuncu bağlandı. ID: " + handler.getPlayerId());
            }
            // İki oyuncu bağlandıktan sonra oyun başlasın
            startGame();

        } catch (IOException e) {
            
            System.err.println("Sunucu hatası: " + e.getMessage());

        }
    }

    // Oyun başlatma fonksiyonu
    private static void startGame() {
        System.out.println("Oyun başlıyor...");

        // Sıra 1. oyuncuda başlasın
        ClientHandler starter = clients.get(0);
        starter.sendMessage("SIRA");

        System.out.println("Sıra Oyuncu 1'de.");
        int currentPlayerIndex = 0;
    }

        public static List<ClientHandler> getClients() {
        return clients;
    }
    
}

// Her Client bağlantısını yönetecek ClientHandler Sınıfı: 
class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private int playerId;

    public ClientHandler(Socket socket, int playerId) {
        this.socket = socket;
        this.playerId = playerId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void sendMessage(String message) {
        if (output != null) {
            output.println(message);
        }
    }
    


    @Override
    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            output.println("Sunucuya bağlandınız. Oyuncu ID'niz: " + playerId);

            String message;
            while ((message = input.readLine()) != null) {
                System.out.println("Oyuncu " + playerId + ": " + message);

                // RESET KOMUTU
                if (message.equals("reset_game")) {
                    System.out.println("Oyuncu " + playerId + " oyunu resetledi.");
                    Server.game = new Game();
                    for (ClientHandler c : Server.getClients()) {
                        c.sendMessage("TAHTA:" + Server.game.serializeBoard());
                    }
                    for (ClientHandler c : Server.getClients()) {
                        c.sendMessage("RESET");
                    }
                    for (ClientHandler c : Server.getClients()) {
                        if (c.getPlayerId() == 1) {
                            c.sendMessage("SIRA");
                        } else {
                            c.sendMessage("Rakip zar atacak...");
                        }
                    }
                    continue;
                }

                // Zar atma isteği
                if (message.equals("roll_dice")) {
                    if (playerId != Server.game.getCurrentPlayer()) {
                        sendMessage("Sıra sizde değil, zar atamazsınız.");
                        continue;
                    }

                    Server.game.rollDice();
                    int d1 = Server.game.getDice1();
                    int d2 = Server.game.getDice2();

                    for (ClientHandler c : Server.getClients()) {
                        if (c.getPlayerId() == playerId) {
                            c.sendMessage("ZARLAR:" + d1 + "," + d2);
                        } else {
                            c.sendMessage("RAKIP_ZAR:" + d1 + "," + d2);
                        }
                        c.sendMessage("TAHTA:" + Server.game.serializeBoard());
                    }

                    // Zar sonrası hamle yapılabilir mi kontrolü
                    if (!Server.game.hasAnyValidMove(playerId)) {
                        sendMessage("Hiç geçerli hamleniz yok. Sıra geçiyor.");
                        Server.game.switchPlayer();
                        for (ClientHandler c : Server.getClients()) {
                            if (c.getPlayerId() == Server.game.getCurrentPlayer()) {
                                c.sendMessage("SIRA");
                            }
                        }
                    }

                    continue;
                }

                // Taş hamlesi
                if (message.startsWith("move:")) {
                    if (playerId != Server.game.getCurrentPlayer()) {
                        sendMessage("Sıra sizde değil, hamle yapamazsınız.");
                        continue;
                    }

                    String[] parts = message.substring(5).split("->");
                    int from = Integer.parseInt(parts[0]);
                    int to = parts[1].equals("OUT") ? (playerId == 1 ? 24 : -1) : Integer.parseInt(parts[1]);

                    int d1 = Server.game.getDice1();
                    int d2 = Server.game.getDice2();

                    boolean success = Server.game.movePiece(from, to, playerId, d1, d2);
                    if (!success) {
                        sendMessage("Geçersiz hamle! Kurallara uymuyor.");
                        continue;
                    }

                    // Tahta güncelle
                    String boardData = Server.game.serializeBoard();
                    for (ClientHandler c : Server.getClients()) {
                        c.sendMessage("TAHTA:" + boardData);
                    }

                    // Oyun bitti mi?
                    if (Server.game.out[1] == 15 || Server.game.out[2] == 15) {
                        int winner = (Server.game.out[1] == 15) ? 1 : 2;
                        for (ClientHandler c : Server.getClients()) {
                            c.sendMessage("OYUN_BITTI: Oyuncu " + winner + " kazandı!");
                        }
                        return;
                    }

                    // Eğer iki zar da kullanıldıysa sıra geçsin
                    if (Server.game.bothDiceUsed()) {
                        Server.game.switchPlayer();
                        for (ClientHandler c : Server.getClients()) {
                            if (c.getPlayerId() == Server.game.getCurrentPlayer()) {
                                c.sendMessage("SIRA");
                            }
                        }
                    }

                    continue;
                }

                // Sohbet mesajları
                synchronized (Server.class) {
                    for (ClientHandler client : Server.getClients()) {
                        client.sendMessage("Oyuncu " + playerId + ": " + message);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("İstemci hatası (Oyuncu " + playerId + "): " + e.getMessage());
        } finally {
            try {
                socket.close();
                System.out.println("Oyuncu " + playerId + " bağlantısı kapatıldı.");
            } catch (IOException e) {
                System.err.println("Bağlantı kapatma hatası.");
            }
        }
    }
 
}