import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static List<ClientHandler> clientList = new ArrayList<>();
    private static Map<String, Integer> scores = new HashMap<>();
    private static int currentTurnIndex = 0;
    private static int lastDiceTotal = 0;

    public static void main(String[] args) {
        System.out.println("Server başlatıldı...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Hata: " + e.getMessage());
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
        private static Random rand = new Random();

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void send(String message) {
            out.println(message);
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("Kullanıcı adınızı giriniz:");
                username = in.readLine();
                System.out.println(username + " bağlandı.");
                scores.put(username, 0);

                synchronized (clientList) {
                    clientList.add(this);
                    if (clientList.size() == 1) {
                        currentTurnIndex = 0;
                        send("Senin sıran!");
                        int d1 = rand.nextInt(6) + 1;
                        int d2 = rand.nextInt(6) + 1;
                        lastDiceTotal = d1 + d2;
                        broadcast("ZAR:" + d1 + "-" + d2);
                    } else {
                        send("Rakibini bekliyorsun...");
                    }
                }

                broadcast("[Sunucu] " + username + " oyuna katıldı.");
                broadcastScores();

                String msg;
                while ((msg = in.readLine()) != null) {
                    if (msg.startsWith("HAMLE:")) {
                        if (clientList.get(currentTurnIndex) == this) {
                            String[] parts = msg.split("->");
                            String[] from = parts[0].substring(6).split("-");
                            String[] to = parts[1].split(" ")[0].split("-");

                            int fr = Integer.parseInt(from[1]);
                            int tr = Integer.parseInt(to[1]);
                            int step = Math.abs(tr - fr);

                            if (step != lastDiceTotal) {
                                send("[Kural] Zarla uyumsuz hamle! Zar: " + lastDiceTotal + ", Hamle: " + step);
                                continue;
                            }

                            broadcast(msg);
                            if (msg.contains("KAZANDI")) {
                                scores.put(username, scores.get(username) + 1);
                                broadcast("[Oyun Bitti] " + username + " oyunu kazandı! Skor: " + scores.get(username));
                                broadcastScores();
                                continue;
                            }

                            synchronized (clientList) {
                                currentTurnIndex = (currentTurnIndex + 1) % clientList.size();
                                ClientHandler next = clientList.get(currentTurnIndex);
                                int d1 = rand.nextInt(6) + 1;
                                int d2 = rand.nextInt(6) + 1;
                                lastDiceTotal = d1 + d2;
                                broadcast("ZAR:" + d1 + "-" + d2);
                                next.send("Senin sıran!");
                                for (int i = 0; i < clientList.size(); i++) {
                                    if (i != currentTurnIndex)
                                        clientList.get(i).send("Rakibini bekliyorsun...");
                                }
                            }
                        } else {
                            send("Sıra sende değil, lütfen bekle.");
                        }
                    } else if (msg.equals("YENIOYUN")) {
                        broadcast("RESET");
                        currentTurnIndex = 0;
                        if (!clientList.isEmpty()) {
                            clientList.get(0).send("Senin sıran!");
                            int d1 = rand.nextInt(6) + 1;
                            int d2 = rand.nextInt(6) + 1;
                            lastDiceTotal = d1 + d2;
                            broadcast("ZAR:" + d1 + "-" + d2);
                            for (int i = 1; i < clientList.size(); i++) {
                                clientList.get(i).send("Rakibini bekliyorsun...");
                            }
                        }
                    } else {
                        broadcast(username + ": " + msg);
                    }
                }
            } catch (IOException e) {
                System.err.println("Bağlantı kesildi: " + username);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {}
                synchronized (clientList) {
                    clientList.remove(this);
                    broadcast("[Sunucu] " + username + " oyundan ayrıldı.");
                }
            }
        }

        private void broadcast(String message) {
            synchronized (clientList) {
                for (ClientHandler c : clientList) {
                    c.send(message);
                }
            }
        }

        private void broadcastScores() {
            broadcast("[Skor Tablosu]");
            for (Map.Entry<String, Integer> entry : scores.entrySet()) {
                broadcast(entry.getKey() + ": " + entry.getValue());
            }
        }
    }
}
