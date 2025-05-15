package protocol;

public class Message {
    // Mesaj türleri enum olarak tanımlanır
    public enum Type {
        NONE,
        CLIENT_IDS,      // Bağlı istemcilerin listesi
        GAME_START,      // Oyun başlangıcı mesajı
        PLAYER_MOVE,     // Oyuncu hamlesi mesajı
        CHAT_MESSAGE,    // Sohbet mesajı
        GAME_STATE,      // Oyun durumu bilgisi
        TO_CLIENT        // Belirli bir istemciye gönderilen mesaj
    }

    // Mesajı oluşturmak için kullanılır: "TYPE#data" formatında string döner
    public static String generate(Type type, String data) {
        return type.name() + "#" + data;
    }

    // Gelen mesajı çözümler, tür ve veri olarak ayırır
    public static ParsedMessage parse(String msg) {
        String[] parts = msg.split("#", 2);
        if(parts.length < 2) return new ParsedMessage(Type.NONE, "");
        try {
            Type type = Type.valueOf(parts[0]);
            return new ParsedMessage(type, parts[1]);
        } catch (IllegalArgumentException e) {
            return new ParsedMessage(Type.NONE, "");
        }
    }

    // ParsedMessage sınıfı, çözümlenmiş mesaj tipini ve verisini tutar
    public static class ParsedMessage {
        public Type type;
        public String data;

        public ParsedMessage(Type type, String data) {
            this.type = type;
            this.data = data;
        }
    }
}
