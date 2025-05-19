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

/*      TEST DÖNGÜSÜ
        // İlk tur test döngüsü: her oyuncu sırayla zar atar
        for (int i = 0; i < clients.size(); i++) {
            ClientHandler player = clients.get(i);

            // Sıradaki oyuncuya zar atması için bilgi gönder
            game.rollDice();

            String diceMessage = "Zarlar: " + game.getDice1() + " ve " + game.getDice2();
            player.sendMessage("Senin sıran. " + diceMessage);

            // Diğer oyuncuya bilgi ver
            for (int j = 0; j < clients.size(); j++) {
                if (j != i) {
                    clients.get(j).sendMessage("Rakip zar attı: " + diceMessage);
                }
            }

            // Simülasyon: bekleme (gerçek projede kullanıcıdan hamle alınır)
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.err.println("Bekleme hatası.");
            }

            game.switchPlayer();
        }

        System.out.println("Test turu tamamlandı.");
        */ 
    }

        public static List<ClientHandler> getClients() {
        return clients;
    }
    
}

// Her Client bağlantısını yönetecek ClientHandler Sınıfı
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

            // Gelen mesajları dinle (şimdilik sadece loglama için)
            String message;
            while ((message = input.readLine()) != null) {
                System.out.println("Oyuncu " + playerId + ": " + message);

                if (message.equals("roll_dice")) {
                
                    // Sadece sırası gelen oyuncudan gelmişse işlem yapılmalı (bu kısmı senin game loop'a bağlayacağız)
                    Server.game.rollDice();
                    int d1 = Server.game.getDice1();
                    int d2 = Server.game.getDice2();
                    // Zarları atan oyuncuya ve diğerine mesaj gönder
                    for (ClientHandler client : Server.getClients()) {
                        if (client.getPlayerId() == this.playerId) {
                            client.sendMessage("ZARLAR:" + d1 + "," + d2);
                        } else {
                            client.sendMessage("RAKIP_ZAR:" + d1 + "," + d2);
                        }
                        client.sendMessage("TAHTA:" + Server.game.serializeBoard());
                    }
                    Server.game.switchPlayer();

                    // Yeni oyuncuya sırayı gönder
                    for (ClientHandler client : Server.getClients()) {
                        if (client.getPlayerId() == Server.game.getCurrentPlayer()) {
                            client.sendMessage("SIRA");
                        }
                    }

                    continue;
                }

                // Mesajı tüm bağlı clientlara gönder (broadcast)
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