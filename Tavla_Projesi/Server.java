package Tavla_Projesi;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server {

    // Bağlantıları yönetecek thread havuzu oluşturuyorum. (Performan Etkili)
    private static ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public static void main(String[] args){
        try {
            // Sunucu soketini 5000 num' lı porta bağlıyorum.
            ServerSocket serverSocket = new ServerSocket(5000);
            System.out.println("Sunucu başladı ve Client bekleniyor...");

            while (true) {

                // İstemci bağlantısı kabul etme
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client Geldi: " + clientSocket.getInetAddress());

                // Her Client için -> Bir thread
                threadPool.execute(new ClientHandler(clientSocket));
            }
        
        } catch (IOException e) {
            
            System.err.println("Sunucu hatası: " + e.getMessage());

        }
    }
}

// Her Client bağlantısını yönetecek ClientHandler Sınıfı
class ClientHandler implements Runnable {
    
    private Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;

    public ClientHandler(Socket socket) {

        this.clientSocket = socket;

    }

    @Override
    public void run() {
        try {
            //Client için input output akışı oluşturdum.
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);

            String message;
            // Client'tan gelen mesajları sürekli dinlettiriyorum.
            while ((message = input.readLine()) != null) {
                System.out.println("Client'tan gelen mesaj: " + message);
                // Client'a geri gönderiyorum.
                output.println("Server mesajınızı aldı: " + message); // Client' a yanıt

            }
        } catch (IOException e) {
            System.err.println("Client bağlantı hatası: " + e.getMessage());
        } finally {
            try {
                
                // Bağlantıyı kapatıyorum.
                if (clientSocket != null) clientSocket.close();
                System.out.println("İstemci bağlantısı kapatıldı.");

            } catch (IOException e) {
                System.err.println("Bağlantı kapatma hatası: " + e.getMessage());
            }
        }
    }
}