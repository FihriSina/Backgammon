package Tavla_Projesi;

import java.util.Random;

public class Game {

    // 24 Tane Hazne (Tavla Tahtası): Her hazne [taş sayısı, oyuncu numarası]
    private int[][] board;
    // Oyuncu Sırası: 1/2
    private int currentPlayer;
    // Zar x2
    private int dice1, dice2;
    
    private Random random;

    // Zar kullanma durumunu kontrol ediyorum
    private boolean zar1Used = false;
    private boolean zar2Used = false;
    
    private int[] bar = new int[3]; // bar[1] → oyuncu 1, bar[2] → oyuncu 2

    int[] out = new int[3]; // out[1] = oyuncu1'in dışarı çıkardığı taşlar, out[2] = oyuncu2

    public Game() {

        board = new int[24][2]; // [taş sayısı, oyuncu numara]
        random = new Random();
        currentPlayer = 1; // Oyuna 1. oyuncu başlasın
        initializeBoard();
    }

    public boolean bothDiceUsed() {
        return zar1Used && zar2Used;
    }

    // Başlangıç pozisyounu
    private void initializeBoard() {
        // Oyuncu 1 taşları
        board[0][0] = 2;  board[0][1] = 1;
        board[11][0] = 5; board[11][1] = 1;
        board[16][0] = 3; board[16][1] = 1;
        board[18][0] = 5; board[18][1] = 1;

        // Oyuncu 2 taşları
        board[23][0] = 2; board[23][1] = 2;
        board[12][0] = 5; board[12][1] = 2;
        board[7][0] = 3;  board[7][1] = 2;
        board[5][0] = 5;  board[5][1] = 2;
    }

    
    // Zar atma Fonksiyonu (Tur başı 2 zar)
    public void rollDice() {
        dice1 = random.nextInt(6) + 1;
        dice2 = random.nextInt(6) + 1;
        zar1Used = false;
        zar2Used = false;
        System.out.println("Oyuncu " + currentPlayer + " zar attı: " + dice1 + " ve " + dice2);

        // Çift zar atıldıysa, iki zar yerine dört kez oynama hakkı olacak şekilde simülasyon yapılabilir
        if (dice1 == dice2) {
            // Öneri: zar1Used ve zar2Used sayacına benzer 4 hak için sayaç yapılabilir (zorunlu değil)
            System.out.println("Çift zar atıldı! 4 hamle hakkı.");
        }
    }

    public String serializeBoard() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 24; i++) {
            sb.append(board[i][0]).append(",").append(board[i][1]);
            if (i < 23) sb.append(";");
        }
        // BAR kısmını da her zaman güncel olarak ekle
        sb.append("|BAR:").append(bar[1]).append(",").append(bar[2]);

        return sb.toString();
    }
    
    // Hamle gerçekleştirme Fonksiyonu 
    public boolean movePiece(int from, int to, int playerId, int zar1, int zar2) {
        // 0. Taş dışarı çıkarma (bearing off)
        if ((playerId == 1 && to == 24) || (playerId == 2 && to == -1)) {
            if (from < 0 || from >= 24) return false;
            if (board[from][0] == 0 || board[from][1] != playerId) return false;
        
            // Tüm taşlar ev bölgesinde mi kontrol et
            int start = (playerId == 1) ? 18 : 0;
            int end = (playerId == 1) ? 24 : 6;
            for (int i = start; i < end; i++) {
                if (i != from && board[i][1] == playerId && board[i][0] > 0) {
                    return false; // evde olmayan taş varsa çıkamaz
                }
            }
        
            int fark = (playerId == 1) ? to - from : from - to;
            if (fark == zar1 && !zar1Used) {
                zar1Used = true;
            } else if (fark == zar2 && !zar2Used) {
                zar2Used = true;
            } else {
                return false;
            }
        
            board[from][0]--;
            if (board[from][0] == 0) board[from][1] = 0;
            out[playerId]++;
            return true;
        }
    
        // 1. Bar’dan çıkış
        if (bar[playerId] > 0) {
            boolean barExit = (playerId == 1) ? to <= 5 : to >= 18;
            if (!barExit || from != -1) return false;
        
            int beklenen1 = (playerId == 1) ? zar1 - 1 : 24 - zar1;
            int beklenen2 = (playerId == 1) ? zar2 - 1 : 24 - zar2;
        
            if (to == beklenen1 && !zar1Used) {
                zar1Used = true;
            } else if (to == beklenen2 && !zar2Used) {
                zar2Used = true;
            } else {
                return false;
            }
        
            if (board[to][0] > 1 && board[to][1] != playerId) return false;
        
            // Rakip taşı kır
            if (board[to][0] == 1 && board[to][1] != playerId) {
                int rakip = board[to][1];
                bar[rakip]++;
                board[to][0] = 0;
                board[to][1] = 0;
            }
        
            bar[playerId]--;
            board[to][0]++;
            board[to][1] = playerId;
            return true;
        }
    
        // 2. Normal taş hareketi
        if (from < 0 || to < 0 || from >= 24 || to >= 24) return false;
        if (board[from][0] == 0 || board[from][1] != playerId) return false;
        if (board[to][0] > 1 && board[to][1] != playerId) return false;
    
        int fark = (playerId == 1) ? to - from : from - to;
        if (fark <= 0) return false;
    
        if (fark == zar1 && !zar1Used) {
            zar1Used = true;
        } else if (fark == zar2 && !zar2Used) {
            zar2Used = true;
        } else {
            return false;
        }
    
        if (board[to][0] == 1 && board[to][1] != playerId) {
            int rakip = board[to][1];
            bar[rakip]++;
            board[to][0] = 0;
            board[to][1] = 0;
        }
    
        board[from][0]--;
        if (board[from][0] == 0) board[from][1] = 0;
    
        if (board[to][0] == 0) board[to][1] = playerId;
        board[to][0]++;
    
        return true;
    }
    
    
    



    public boolean isGameOver() {
        // Oyuncu 1 için
        boolean p1Finished = true;
        boolean p2Finished = true;

        for (int i = 0; i < 24; i++) {
            if (board[i][1] == 1 && board[i][0] > 0) {
                p1Finished = false;
            }
            if (board[i][1] == 2 && board[i][0] > 0) {
                p2Finished = false;
            }
        }

        return p1Finished || p2Finished;
    }

    public int getWinner() {
        // Bu metot sadece oyun bittiyse çağrılmalı
        for (int i = 0; i < 24; i++) {
            if (board[i][1] == 1 && board[i][0] > 0) return 2;
            if (board[i][1] == 2 && board[i][0] > 0) return 1;
        }
        return currentPlayer;
    }

    // Oyuncu Sırası Değiştirme
    public void switchPlayer() {
        currentPlayer = (currentPlayer == 1) ? 2 : 1; // Oyuncu sırasını değiştir
        System.out.println("Sıra Oyuncu " + currentPlayer + "'da");
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public int getDice1() {
        return dice1;
    }

    public int getDice2() {
        return dice2;
    }

    public void printBoard() {
        System.out.println("----- Tahta Durumu -----");
        for (int i = 0; i < 24; i++) {
            if ( board[i][0] > 0) {
                System.out.println("Hazne " + i + ": " + board[i][0] + " taş (Oyuncu " + board[i][1] + ")");
            }
        }
        System.out.println("-------------------------");
    }
}