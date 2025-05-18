package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private int playerId;

    public ClientHandler(Socket socket, int playerId) {
        this.socket = socket;
        this.playerId = playerId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("CONNECTED " + playerId);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Oyuncu " + playerId + ": " + inputLine);

                // Oyuncudan gelen mesajı diğer oyuncuya gönder
                Server.broadcast("PLAYER" + playerId + ": " + inputLine, playerId);
            }

        } catch (Exception e) {
            System.out.println("Oyuncu " + playerId + " bağlantı hatası: " + e.getMessage());
        }
    }
}
