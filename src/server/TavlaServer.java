// Proje: Tavla Oyunu (Ağ Üzerinden)
// Adım 1: Klasör ve Temel Sınıf Yapısı

// server/TavlaServer.java
package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class TavlaServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Tavla sunucusu başlatıldı. Bekleniyor...");

        Socket player1 = serverSocket.accept();
        System.out.println("Oyuncu 1 bağlandı: " + player1.getInetAddress());

        Socket player2 = serverSocket.accept();
        System.out.println("Oyuncu 2 bağlandı: " + player2.getInetAddress());

        // Oyunculara "oyun başlıyor" mesajı gönder
        PrintWriter out1 = new PrintWriter(player1.getOutputStream(), true);
        PrintWriter out2 = new PrintWriter(player2.getOutputStream(), true);

        out1.println("Oyun başlıyor. Sen Oyuncu 1'sin.");
        out2.println("Oyun başlıyor. Sen Oyuncu 2'sin.");

        // Gelecek: oyun mantığı, mesaj dinleme, yönlendirme
        serverSocket.close();
    }
}
