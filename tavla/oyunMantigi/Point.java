package tavla.oyunMantigi;

import java.io.Serializable;

public class Point implements Serializable {
    private int count;
    private int owner;

    public Point(int count, int owner) {
        this.count = count;
        this.owner = owner;
    }

    public int getCount() {
        return count;
    }

    public int getOwner() {
        return owner;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public void increaseCount() {
        this.count++;
    }

    public void decreaseCount() {
        if (this.count > 0) this.count--;
    }
}
