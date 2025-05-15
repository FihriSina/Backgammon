package client;

import java.io.*;
import java.net.*;

public class TavlaClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private GameGUI gui;

    public TavlaClient(String serverAddress, int port) throws IOException {
        // Sunucuya bağlan
        socket = new Socket(serverAddress, port);

        // Sunucudan veri okumak için BufferedReader oluştur
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Sunucuya veri yazmak için PrintWriter oluştur
        out = new PrintWriter(socket.getOutputStream(), true);

        // GUI'yi başlat
        gui = new GameGUI();

        // GUI üzerindeki butona tıklandığında mesaj gönder
        gui.addSendListener(() -> {
            String msg = gui.getMessageInput();
            if (!msg.isEmpty()) {
                out.println(msg);          // Mesajı sunucuya gönder
                gui.clearMessageInput();   // Mesaj kutusunu temizle
            }
        });
    }

    // Sunucudan gelen mesajları dinle ve GUI'ye göster
    public void startListening() {
        new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    gui.showMessage(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        try {
            TavlaClient client = new TavlaClient("localhost", 5000);
            client.startListening();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
