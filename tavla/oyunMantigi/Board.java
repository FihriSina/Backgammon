package tavla.oyunMantigi;

import java.io.Serializable;

public class Board implements Serializable {
    public static final int POINT_COUNT = 24;
    private Point[] points;
    private int[] bar;
    private int[] bearOff;

    public Board() {
        points = new Point[POINT_COUNT];
        for (int i = 0; i < POINT_COUNT; i++) {
            points[i] = new Point(0, -1);
        }

        bar = new int[2];
        bearOff = new int[2];

        initializeBoard();
    }

    private void initializeBoard() {
        points[0] = new Point(2, 0);
        points[11] = new Point(5, 0);
        points[16] = new Point(3, 0);
        points[18] = new Point(5, 0);

        points[23] = new Point(2, 1);
        points[12] = new Point(5, 1);
        points[7]  = new Point(3, 1);
        points[5]  = new Point(5, 1);
    }

    public Point getPoint(int index) {
        return points[index];
    }

    public void movePiece(int from, int to, int playerId) {
        if (from >= 0) {
            points[from].decreaseCount();
            if (points[from].getCount() == 0) {
                points[from].setOwner(-1);
            }
        }

        if (points[to].getCount() == 0 || points[to].getOwner() == playerId) {
            points[to].increaseCount();
            points[to].setOwner(playerId);
        } else if (points[to].getCount() == 1) {
            int opponent = points[to].getOwner();
            addToBar(opponent);
            points[to].setOwner(playerId);
            points[to].setCount(1);
        }
    }

    public int getBarCount(int playerId) {
        return bar[playerId];
    }

    public void removeFromBar(int playerId) {
        if (bar[playerId] > 0) {
            bar[playerId]--;
        }
    }

    public void addToBar(int playerId) {
        bar[playerId]++;
    }

    public int getBearOffCount(int playerId) {
        return bearOff[playerId];
    }

    public void bearOff(int playerId) {
        bearOff[playerId]++;
    }
}
