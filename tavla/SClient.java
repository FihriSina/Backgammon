package tavla;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class SClient extends Thread {
    private Socket socket;
    private int clientId;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private GameLogic game;
    private List<SClient> allClients;

    public SClient(Socket socket, int clientId, GameLogic game, List<SClient> allClients) {
        this.socket = socket;
        this.clientId = clientId;
        this.game = game;
        this.allClients = allClients;
    }

    public void run() {
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();  // Önemli: Akışı temizle
            input = new ObjectInputStream(socket.getInputStream());

            sendMessage(new Message(Message.MessageType.CONNECT, -1, "Hoş geldin, oyuncu #" + clientId));

            while (true) {
                try {
                    Message msg = (Message) input.readObject(); // Güvenli okuma
                    
                    if (msg == null) break; // Bağlantı kapandığında çık
                    
                    switch (msg.getType()) {
                        case ROLL_DICE:
                            if (game.getCurrentPlayer() == clientId) {
                                int[] dice = game.rollDice();
                                broadcast(new Message(Message.MessageType.ROLL_DICE, clientId, dice));
                            }
                            break;
                        case MOVE:
                            int[] move = (int[]) msg.getData();
                            if (move != null && move.length == 2) { // Güvenlik kontrolü
                                game.move(move[0], move[1]);
                                broadcast(new Message(Message.MessageType.UPDATE, clientId, game.getBoard()));
                            }
                            break;
                        case ENTER_BAR:
                            if (msg.getData() instanceof Integer) {
                                int diceVal = (int) msg.getData();
                                game.enterFromBar(diceVal);
                                broadcast(new Message(Message.MessageType.UPDATE, clientId, game.getBoard()));
                            }
                            break;
                        case TRY_BEAR_OFF:
                            if (msg.getData() instanceof Integer) {
                                int from = (int) msg.getData();
                                game.tryBearOff(from);
                                broadcast(new Message(Message.MessageType.UPDATE, clientId, game.getBoard()));
                            }
                            break;
                        case NEXT_TURN:
                            if (game.turnFinished()) {
                                game.nextTurn();
                                broadcast(new Message(Message.MessageType.INFO, -1, "Sıra oyuncu #" + game.getCurrentPlayer()));
                            }
                            break;
                        case IS_GAME_OVER:
                            if (game.isGameOver()) {
                                int winner = game.getWinner();
                                broadcast(new Message(Message.MessageType.INFO, -1, "Oyun bitti! Kazanan: Oyuncu #" + winner));
                            }
                            break;
                        case CHAT:
                            broadcast(msg);
                            break;
                    }

                } catch (EOFException e) {
                    System.err.println("Client #" + clientId + " bağlantısı kapandı.");
                    break;
                } catch (ClassNotFoundException | IOException e) {
                    System.err.println("Client #" + clientId + " bağlantı hatası.");
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Client #" + clientId + " başlatma hatası.");
        } finally {
            try { socket.close(); } catch (IOException e) { System.err.println("Soket kapatma hatası."); }
        }
    }

    private void sendMessage(Message msg) {
        try {
            output.writeObject(msg);
            output.flush(); // Mesajın anında gitmesini sağla
        } catch (IOException e) {
            System.err.println("Mesaj gönderme hatası: " + e.getMessage());
        }
    }

    private void broadcast(Message msg) {
        for (SClient c : allClients) {
            c.sendMessage(msg);
        }
    }
}