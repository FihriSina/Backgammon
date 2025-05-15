package tavla;

import java.io.Serializable;
import java.util.Random;

public class GameLogic implements Serializable {

    private Board board;
    private int currentPlayer; // 0 veya 1
    private int[] dice;        // Zar sonuçları 
    private boolean[] diceUsed; // Zarların kullanılıp kullanılmadığı
    private Random rand;

    public GameLogic() {
        board = new Board();
        currentPlayer = 0;
        dice = new int[2];
        diceUsed = new boolean[2];
        rand = new Random();
    }

    public Board getBoard() {
        return board;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public int[] rollDice() {
        dice[0] = rand.nextInt(6) + 1;
        dice[1] = rand.nextInt(6) + 1;
        diceUsed[0] = false;
        diceUsed[1] = false;
        return dice;
    }

    public boolean isValidMove(int from, int to) {
        Point pFrom = board.getPoint(from);
        Point pTo = board.getPoint(to);

        if (pFrom.getOwner() != currentPlayer || pFrom.getCount() == 0)
            return false;

        if (pTo.getOwner() != -1 && pTo.getOwner() != currentPlayer && pTo.getCount() > 1)
            return false;

        int distance = Math.abs(to - from);
        return (distance == dice[0] && !diceUsed[0]) || (distance == dice[1] && !diceUsed[1]);
    }

    public boolean move(int from, int to) {
        if (!isValidMove(from, to))
            return false;

        int distance = Math.abs(to - from);

        if (distance == dice[0] && !diceUsed[0]) {
            diceUsed[0] = true;
        } else if (distance == dice[1] && !diceUsed[1]) {
            diceUsed[1] = true;
        }

        board.movePiece(from, to, currentPlayer);
        return true;
    }

    public boolean turnFinished() {
        return diceUsed[0] && diceUsed[1];
    }

    public void nextTurn() {
        currentPlayer = 1 - currentPlayer;
        rollDice();
    }

    public boolean hasPieceOnBar(int playerId) {
        return board.getBarCount(playerId) > 0;
    }

    public boolean canEnterFromBar(int diceValue) {
        int entryPoint = getBarEntryPoint(currentPlayer, diceValue);
        Point point = board.getPoint(entryPoint);

        return point.getCount() < 2 || point.getOwner() == currentPlayer;
    }

    public boolean enterFromBar(int diceValue) {
        if (!canEnterFromBar(diceValue)) return false;

        int entryPoint = getBarEntryPoint(currentPlayer, diceValue);
        Point point = board.getPoint(entryPoint);

        if (point.getCount() == 1 && point.getOwner() != currentPlayer) {
            int opponent = point.getOwner();
            board.removeFromBar(currentPlayer);
            point.setOwner(currentPlayer);
            point.setCount(1);
            board.addToBar(opponent);
        } else {
            board.movePiece(-1, entryPoint, currentPlayer);
            board.removeFromBar(currentPlayer);
        }

        if (!diceUsed[0] && diceValue == dice[0]) diceUsed[0] = true;
        else if (!diceUsed[1] && diceValue == dice[1]) diceUsed[1] = true;

        return true;
    }

    private int getBarEntryPoint(int playerId, int diceValue) {
        return playerId == 0 ? diceValue - 1 : 24 - diceValue;
    }

    public boolean canMakeAnyMove() {
        if (hasPieceOnBar(currentPlayer)) {
            return canEnterFromBar(dice[0]) || canEnterFromBar(dice[1]);
        }

        for (int i = 0; i < Board.POINT_COUNT; i++) {
            if (board.getPoint(i).getOwner() == currentPlayer) {
                int to1 = currentPlayer == 0 ? i + dice[0] : i - dice[0];
                int to2 = currentPlayer == 0 ? i + dice[1] : i - dice[1];

                if (to1 >= 0 && to1 < 24 && isValidMove(i, to1)) return true;
                if (to2 >= 0 && to2 < 24 && isValidMove(i, to2)) return true;
            }
        }

        return false;
    }

    public boolean canBearOff(int playerId) {
        int start = playerId == 0 ? 18 : 0;
        int end = playerId == 0 ? 24 : 6;

        for (int i = 0; i < Board.POINT_COUNT; i++) {
            if (i < start || i >= end) {
                Point p = board.getPoint(i);
                if (p.getOwner() == playerId && p.getCount() > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean tryBearOff(int from) {
        if (!canBearOff(currentPlayer)) return false;

        int distance = currentPlayer == 0 ? 23 - from : from;
        int diceVal = -1;

        if (!diceUsed[0] && dice[0] == distance + 1) {
            diceUsed[0] = true;
            diceVal = dice[0];
        } else if (!diceUsed[1] && dice[1] == distance + 1) {
            diceUsed[1] = true;
            diceVal = dice[1];
        } else if (!diceUsed[0] && dice[0] > distance + 1) {
            if (isHighestPoint(from)) {
                diceUsed[0] = true;
                diceVal = dice[0];
            }
        } else if (!diceUsed[1] && dice[1] > distance + 1) {
            if (isHighestPoint(from)) {
                diceUsed[1] = true;
                diceVal = dice[1];
            }
        }

        if (diceVal != -1) {
            board.getPoint(from).decreaseCount();
            if (board.getPoint(from).getCount() == 0) {
                board.getPoint(from).setOwner(-1);
            }
            board.bearOff(currentPlayer);
            return true;
        }

        return false;
    }

    private boolean isHighestPoint(int from) {
        int start = currentPlayer == 0 ? 18 : 0;
        int end = currentPlayer == 0 ? 24 : 6;

        for (int i = start; i < end; i++) {
            if (currentPlayer == 0 && i > from && board.getPoint(i).getOwner() == 0) return false;
            if (currentPlayer == 1 && i < from && board.getPoint(i).getOwner() == 1) return false;
        }
        return true;
    }

    public boolean isGameOver() {
        return board.getBearOffCount(0) == 15 || board.getBearOffCount(1) == 15;
    }

    public int getWinner() {
        if (board.getBearOffCount(0) == 15) return 0;
        if (board.getBearOffCount(1) == 15) return 1;
        return -1;
    }
}
