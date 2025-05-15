// GamePanel.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel implements MouseListener {
    private final int ROWS = 2;
    private final int COLS = 15;
    private int[][] board = new int[ROWS][COLS];

    private int selectedRow = -1;
    private int selectedCol = -1;

    private ClientGUI client;

    public GamePanel(ClientGUI client) {
        this.client = client;
        setBackground(Color.DARK_GRAY);
        addMouseListener(this);

        resetBoard();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int cellWidth = getWidth() / COLS;
        int cellHeight = (getHeight() - 100) / ROWS;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int x = col * cellWidth;
                int y = row * cellHeight + 100;

                g.setColor(Color.ORANGE);
                g.fillRect(x, y, cellWidth, cellHeight);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, cellWidth, cellHeight);

                if (row == selectedRow && col == selectedCol) {
                    g.setColor(Color.RED);
                    g.drawRect(x + 2, y + 2, cellWidth - 4, cellHeight - 4);
                }

                if (board[row][col] == 1) {
                    g.setColor(Color.BLACK);
                    g.fillOval(x + 10, y + 10, 20, 20);
                } else if (board[row][col] == 2) {
                    g.setColor(Color.WHITE);
                    g.fillOval(x + 10, y + 10, 20, 20);
                    g.setColor(Color.BLACK);
                    g.drawOval(x + 10, y + 10, 20, 20);
                }
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int cellWidth = getWidth() / COLS;
        int cellHeight = (getHeight() - 100) / ROWS;

        int col = e.getX() / cellWidth;
        int row = (e.getY() - 100) / cellHeight;

        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return;

        if (!ClientGUI.isMyTurn()) {
            JOptionPane.showMessageDialog(this, "Sıra sende değil!");
            return;
        }

        if (selectedRow == -1 && board[row][col] != 0) {
            selectedRow = row;
            selectedCol = col;
        } else if (selectedRow != -1) {
            if (board[row][col] == 0) {
                board[row][col] = board[selectedRow][selectedCol];
                board[selectedRow][selectedCol] = 0;

                if (checkVictory()) {
                    String move = selectedRow + "-" + selectedCol + "->" + row + "-" + col + " KAZANDI";
                    client.sendMove(move);
                } else {
                    String move = selectedRow + "-" + selectedCol + "->" + row + "-" + col;
                    client.sendMove(move);
                }

                selectedRow = -1;
                selectedCol = -1;
            } else {
                selectedRow = row;
                selectedCol = col;
            }
        }
        repaint();
    }

    public void applyMove(String moveStr) {
        try {
            String[] parts = moveStr.split("->");
            String[] from = parts[0].split("-");
            String[] to = parts[1].split("-");

            int fr = Integer.parseInt(from[0]);
            int fc = Integer.parseInt(from[1]);
            int tr = Integer.parseInt(to[0]);
            int tc = Integer.parseInt(to[1]);

            board[tr][tc] = board[fr][fc];
            board[fr][fc] = 0;

            repaint();
        } catch (Exception e) {
            System.err.println("Hamle çözümlenemedi: " + moveStr);
        }
    }

    public void resetBoard() {
        for (int i = 0; i < COLS; i++) {
            board[0][i] = 1;
            board[1][i] = 2;
        }
        selectedRow = -1;
        selectedCol = -1;
        repaint();
    }

    private boolean checkVictory() {
        int playerStone = (selectedRow == 0) ? 1 : 2;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board[r][c] == playerStone) {
                    return false;
                }
            }
        }
        return true;
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}
