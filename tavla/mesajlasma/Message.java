package tavla.mesajlasma;

import java.io.Serializable;

public class Message implements Serializable {

    public enum MessageType {
        CONNECT,
        ROLL_DICE,
        MOVE,
        ENTER_BAR,
        TRY_BEAR_OFF,
        UPDATE,
        NEXT_TURN,
        CHAT,
        INFO,
        IS_GAME_OVER
    }

    private MessageType type;
    private int senderId;
    private Object data;

    public Message(MessageType type, int senderId, Object data) {
        this.type = type;
        this.senderId = senderId;
        this.data = data;
    }

    public MessageType getType() {
        return type;
    }

    public int getSenderId() {
        return senderId;
    }

    public Object getData() {
        return data;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
