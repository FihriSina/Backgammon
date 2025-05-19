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

    public Game() {

        board = new int[24][2]; // [taş sayısı, oyuncu numara]
        random = new Random();
        currentPlayer = 1; // Oyuna 1. oyuncu başlasın
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
        dice1 = random.nextInt(6) + 1; // 1-6 arası zar
        dice2 = random.nextInt(6) + 1; 
        System.out.println("Oyuncu " + currentPlayer + " zar attı: " + dice1 + " ve " + dice2);
    }

    public String serializeBoard() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 24; i++) {
            sb.append(board[i][0]).append(",").append(board[i][1]);
            if (i < 23) sb.append(";");
        }
        return sb.toString();
    }


    // Hamle gerçekleştirme Fonksiyonu 
    public boolean movePiece(int from, int diceValue) {
        int to = currentPlayer == 1 ? from + diceValue : from - diceValue; // Oyuncu 1 ise sola, 2 ise sağa

        // Tahta Sınır Kontrol
        if(to < 0 || to >= 24) {
            System.out.println("Tahta Sınırının Dışındasın Kardeş, Nereye gidiysun Gülüm");
            return false;
        }

        // Başlangıç Pozisyonunda Taşın var mı ki
        if(board[from][0] == 0 || board[from][1] != currentPlayer) {
            System.out.println("Başlangıç Pozisyonunda Taşın yok ki, Nereye gidiysun Gülüm");
            return false;
        }

        // Gideceği yer boş yada aynı kardeşin taşı olmalı
        if(board[to][0] > 0 && board[to][1] != currentPlayer) {
            System.out.println("Gideceğin yerde karşı kardeş var, Nereye gidiysun Gülüm");
            return false;
        }

        // Hamleyi Uygula
        board[from][0]--; // Başlangıç pozisyonundaki taş sayısını azalt
        if(board[from][0] == 0) board[from][1] = 0; // Başlangıç pozisyonu boşsa oyuncu numarasını sıfırla

        if (board[to][0] == 0) board[to][1] = currentPlayer; // Gideceği yer boşsa oyuncu numarasını ata
        board[to][0]++; // Gideceği yerin taş sayısını artır

        System.out.println("Oyuncu " + currentPlayer + ": " + from + " -> " + to);
        return true;
            
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
