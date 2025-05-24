package Tavla_Projesi;

import java.util.Random;

public class Game {

    private int[][] board;
    private int currentPlayer;
    private int dice1, dice2;
    
    private boolean zar1Used = false;
    private boolean zar2Used = false;
    private boolean zar3Used = false;
    private boolean zar4Used = false;
    private boolean doubleRoll = false;
    private int doubleValue = -1;

    private int[] bar = new int[3];
    int[] out = new int[3];

    private Random random;

    public Game() {
        board = new int[24][2];
        random = new Random();
        currentPlayer = 1;
        initializeBoard();
    }

    private void initializeBoard() {
        // 23 numaralı hazineye 2 tane siyah taş koy (oyuncu 1)
        board[23][0] = 5;    // Taş sayısı
        board[23][1] = 2;    // Oyuncu numarası (1: siyah)

        // 12 numaralı hazineye 5 tane siyah taş koy
        board[12][0] = 5;
        board[12][1] = 1;

        // 7 numaralı hazineye 3 tane siyah taş koy
        board[7][0] = 3;
        board[7][1] = 1;

        // 5 numaralı hazineye 5 tane siyah taş koy
        board[5][0] = 5;
        board[5][1] = 1;

        // 0 numaralı hazineye 2 tane beyaz taş koy (oyuncu 2)
        board[0][0] = 2;
        board[0][1] = 2;

        // 11 numaralı hazineye 5 tane beyaz taş koy
        board[11][0] = 5;
        board[11][1] = 2;

        // 16 numaralı hazineye 3 tane beyaz taş koy
        board[16][0] = 3;
        board[16][1] = 2;

        // 18 numaralı hazineye 5 tane beyaz taş koy*
        board[18][0] = 2;
        board[18][1] = 1;
    }


    public void rollDice() {
        dice1 = random.nextInt(6) + 1;
        dice2 = random.nextInt(6) + 1;
        zar1Used = false;
        zar2Used = false;

        if (dice1 == dice2) {
            zar3Used = false;
            zar4Used = false;
            doubleRoll = true;
            doubleValue = dice1;
        } else {
            doubleRoll = false;
        }
    }

    public boolean movePiece(int from, int to, int playerId, int zar1, int zar2) {
        int fark = (playerId == 1) ? to - from : from - to;

        // Bearing off
        if ((playerId == 1 && to == 24) || (playerId == 2 && to == -1)) {
            if (from < 0 || from >= 24 || board[from][0] == 0 || board[from][1] != playerId)
                return false;

            int start = (playerId == 1) ? 18 : 0;
            int end = (playerId == 1) ? 24 : 6;
            for (int i = start; i < end; i++) {
                if (i != from && board[i][1] == playerId && board[i][0] > 0) return false;
            }

            int maxPoint = getMaxOccupiedPointInHome(playerId);
            if ((fark == zar1 && !zar1Used) || (fark >= maxPoint && !zar1Used)) {
                zar1Used = true;
            } else if ((fark == zar2 && !zar2Used) || (fark >= maxPoint && !zar2Used)) {
                zar2Used = true;
            } else {
                return false;
            }

            board[from][0]--;
            if (board[from][0] == 0) board[from][1] = 0;
            out[playerId]++;
            return true;
        }

        // Bar'dan çıkış
        if (bar[playerId] > 0) {
            if (from != -1) return false;

            int beklenen1 = (playerId == 1) ? zar1 - 1 : 24 - zar1;
            int beklenen2 = (playerId == 1) ? zar2 - 1 : 24 - zar2;

            if (to != beklenen1 && to != beklenen2) return false;
            if (board[to][0] > 1 && board[to][1] != playerId) return false;

            if (to == beklenen1 && !zar1Used) zar1Used = true;
            else if (to == beklenen2 && !zar2Used) zar2Used = true;
            else return false;

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

        // Normal hareket
        if (from < 0 || to < 0 || from >= 24 || to >= 24) return false;
        if (board[from][0] == 0 || board[from][1] != playerId) return false;
        if (board[to][0] > 1 && board[to][1] != playerId) return false;
        if (fark <= 0) return false;

        if (doubleRoll) {
            if (!zar1Used) zar1Used = true;
            else if (!zar2Used) zar2Used = true;
            else if (!zar3Used) zar3Used = true;
            else if (!zar4Used) zar4Used = true;
            else return false;
        } else {
            if (fark == zar1 && !zar1Used) zar1Used = true;
            else if (fark == zar2 && !zar2Used) zar2Used = true;
            else return false;
        }

        if (board[to][0] == 1 && board[to][1] != playerId) {
            int rakip = board[to][1];
            bar[rakip]++;
            board[to][0] = 0;
            board[to][1] = 0;
        }

        board[from][0]--;
        if (board[from][0] == 0) board[from][1] = 0;
        board[to][0]++;
        board[to][1] = playerId;

        return true;
    }

    private int getMaxOccupiedPointInHome(int playerId) {
        int max = 0;
        if (playerId == 1) {
            for (int i = 18; i < 24; i++) {
                if (board[i][1] == playerId && board[i][0] > 0) {
                    max = Math.max(max, 24 - i);
                }
            }
        } else {
            for (int i = 5; i >= 0; i--) {
                if (board[i][1] == playerId && board[i][0] > 0) {
                    max = Math.max(max, i + 1);
                }
            }
        }
        return max;
    }

    public boolean hasAnyValidMove(int playerId) {
        if (bar[playerId] > 0) {
            int[] hedefler = new int[]{
                (playerId == 1) ? dice1 - 1 : 24 - dice1,
                (playerId == 1) ? dice2 - 1 : 24 - dice2
            };
            for (int to : hedefler) {
                if (to >= 0 && to < 24) {
                    if (board[to][0] <= 1 || board[to][1] == playerId) return true;
                }
            }
            return false;
        }

        for (int from = 0; from < 24; from++) {
            if (board[from][0] > 0 && board[from][1] == playerId) {
                int[] hedefler = new int[]{
                    (playerId == 1) ? from + dice1 : from - dice1,
                    (playerId == 1) ? from + dice2 : from - dice2
                };
                for (int to : hedefler) {
                    if ((to >= 0 && to < 24 && (board[to][0] <= 1 || board[to][1] == playerId)) ||
                        (playerId == 1 && to == 24) ||
                        (playerId == 2 && to == -1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean bothDiceUsed() {
        return zar1Used && zar2Used && (!doubleRoll || (zar3Used && zar4Used));
    }

    public String serializeBoard() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 24; i++) {
            sb.append(board[i][0]).append(",").append(board[i][1]);
            if (i < 23) sb.append(";");
        }
        sb.append("|BAR:").append(bar[1]).append(",").append(bar[2]);
        return sb.toString();
    }

    public void switchPlayer() {
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
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

    public boolean isGameOver() {
        return out[1] == 15 || out[2] == 15;
    }

    public int getWinner() {
        return (out[1] == 15) ? 1 : 2;
    }

    public void printBoard() {
        System.out.println("----- Tahta Durumu -----");
        for (int i = 0; i < 24; i++) {
            if (board[i][0] > 0) {
                System.out.println("Hazne " + i + ": " + board[i][0] + " taş (Oyuncu " + board[i][1] + ")");
            }
        }
        System.out.println("-------------------------");
    }
}
