package Tavla_Projesi;                  // Paket Bilgisi

import java.util.Random;                // Rastgele sayı üretimi için gerekli kütüphane

public class Game {                     // Tavla oyunu mantığını içeren sınıf

    private int[][] board;              // 24 hazineyi tutar. [taş sayısı] [oyuncu numarası] 
    private int currentPlayer;          // Sıra hangi oyuncuda
    private int dice1, dice2;           // Zar Değerleri
    
    private boolean zar1Used = false;   // Zar 1 kullanıldı mı?
    private boolean zar2Used = false;   // Zar 2 kullanıldı mı?
    private boolean zar3Used = false;   // Zar 3 kullanıldı mı? (Çift zar)
    private boolean zar4Used = false;   // Zar 4 kullanıldı mı? (Çift zar)
    private boolean doubleRoll = false; // Çift zar atıldı mı?
    private int doubleValue = -1;       // Çift zarın değeri

    private int[] bar = new int[3];     // Bar'daki taş sayısı [0: boş, 1: siyah, 2: beyaz]
    int[] out = new int[3];         // Oyuncuların dışarı attığı taş sayısı [0: boş, 1: siyah, 2: beyaz]

    private Random random;              // Zar için ratgele sayı üreteci

    public Game() {                     // Oyun başlarken tahta, zar, oyuncu ilk hale gelir.
        board = new int[24][2];
        random = new Random();
        currentPlayer = 1;
        initializeBoard();
    }

    private void initializeBoard() { // Tahtanın Başlangıç Durumu (Taşların konumu)
        // board[taş sayısı][oyuncu numarası] -> Oyuncu numarası 1: siyah, 2: beyaz

        // 23 numaralı hazneye 5 tane siyah taş koy (oyuncu 1)
        board[23][0] = 5;    
        board[23][1] = 2;   // Beyaz Oyuncu  

        // 12 numaralı hazneye 5 tane siyah taş koy
        board[12][0] = 5;   
        board[12][1] = 1;    // Siyah Oyuncu

        // 7 numaralı hazneye 3 tane siyah taş koy
        board[7][0] = 3;
        board[7][1] = 1;     // Siyah Oyuncu

        // 5 numaralı hazneye 5 tane siyah taş koy
        board[5][0] = 5;
        board[5][1] = 1;     // Siyah Oyuncu

        // 0 numaralı hazneye 2 tane beyaz taş koy (oyuncu 2)
        board[0][0] = 2;
        board[0][1] = 2;     // Beyaz Oyuncu

        // 11 numaralı hazneye 5 tane beyaz taş koy
        board[11][0] = 5;
        board[11][1] = 2;    // Beyaz Oyuncu

        // 16 numaralı hazneye 3 tane beyaz taş koy
        board[16][0] = 3;
        board[16][1] = 2;    // Beyaz Oyuncu

        // 18 numaralı hazneye 2 tane beyaz taş koy
        board[18][0] = 2;
        board[18][1] = 1;    // Siyah Oyuncu
    }

    public void rollDice() {  // Zarları atma fonksiyonu
        dice1 = random.nextInt(6) + 1;
        dice2 = random.nextInt(6) + 1;
        
        zar1Used = false;     // Zarlar atıldıktan sonra kullanılmadı olarak işaretlenir.
        zar2Used = false;

        if (dice1 == dice2) { // Çift Zar
            zar3Used = false;
            zar4Used = false;
            doubleRoll = true;
            doubleValue = dice1;
        } else {
            doubleRoll = false;
        }
    }

    public boolean movePiece(int from, int to, int playerId, int zar1, int zar2) { // Taş hareket ettirme fonksiyonu
        int fark = (playerId == 1) ? to - from : from - to;// from= başlangıç hazne, to= hedef hazne, fark= taş kaç hane ilerleyecek
                                                           // Yön Hesabı: Oyuncu 1 sağa, Oyuncu 2 sola 
        // Taşı Dışarı Çıkarma: Siyah: 24 ,Beyaz: -1 konumu kontrol edilir.
        if ((playerId == 1 && to == 24) || (playerId == 2 && to == -1)) {
            if (from < 0 || from >= 24 || board[from][0] == 0 || board[from][1] != playerId)
                return false;

            // Tahtada(evde) taş kaldı mı?
            int start = (playerId == 1) ? 18 : 0;
            int end = (playerId == 1) ? 24 : 6;
            for (int i = start; i < end; i++) {
                if (i != from && board[i][1] == playerId && board[i][0] > 0) return false;
            }

            //  Zar değeri ile  taş dışarı çıkarılabilir mi?
            int maxPoint = getMaxOccupiedPointInHome(playerId);
            if ((fark == zar1 && !zar1Used) || (fark >= maxPoint && !zar1Used)) {
                zar1Used = true;
            } else if ((fark == zar2 && !zar2Used) || (fark >= maxPoint && !zar2Used)) {
                zar2Used = true;
            } else {
                return false;
            }
            // Taş dışarı çıkar ve haznedeki sayısı azalır Çıkan taş sayısı artar.
            board[from][0]--;
            if (board[from][0] == 0) board[from][1] = 0;
            out[playerId]++;
            return true;
        }

        // Bar'dan çıkış (Kırık taş oyuna giriyor)
        if (bar[playerId] > 0) { // Sadece bar daki taş hareket edilebilir.
            if (from != -1) return false;

            // Zara göre kırık taş oyuna girer ancak rakip varsa giremez.
            int beklenen1 = (playerId == 1) ? zar1 - 1 : 24 - zar1;
            int beklenen2 = (playerId == 1) ? zar2 - 1 : 24 - zar2;
            if (to != beklenen1 && to != beklenen2) return false;
            if (board[to][0] > 1 && board[to][1] != playerId) return false;

            // Kullanılan zarları işaretle
            if (to == beklenen1 && !zar1Used) zar1Used = true; 
            else if (to == beklenen2 && !zar2Used) zar2Used = true;
            else return false;

            // Rakip tek taşsa onu bar a yolla.
            if (board[to][0] == 1 && board[to][1] != playerId) {
                int rakip = board[to][1];
                bar[rakip]++;
                board[to][0] = 0;
                board[to][1] = 0;
            }

            // Bar da taş azalır, hedef hazneye taş eklenir.
            bar[playerId]--;
            board[to][0]++;
            board[to][1] = playerId;
            return true;
        }

        // Normal hareketler
        // Tahta dışı, Yanlış Taş, Rakip Bloğu, Yanlış Yön Engelleyici
        if (from < 0 || to < 0 || from >= 24 || to >= 24) return false;
        if (board[from][0] == 0 || board[from][1] != playerId) return false;
        if (board[to][0] > 1 && board[to][1] != playerId) return false;
        if (fark <= 0) return false;

        //Çift Zar için 4 hamle hakkı, Normal zar için 2 hak
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

        // Hedef haznede rakibin tek taş ise bar a yolla
        if (board[to][0] == 1 && board[to][1] != playerId) {
            int rakip = board[to][1];
            bar[rakip]++;
            board[to][0] = 0;
            board[to][1] = 0;
        }

        // Taşı hareket ettir - Taş Sayısı - Id döner
        board[from][0]--;
        if (board[from][0] == 0) board[from][1] = 0;
        board[to][0]++;
        board[to][1] = playerId;

        return true;
    }

    private int getMaxOccupiedPointInHome(int playerId) {
        int max = 0; // En ileride taşın çıkışa uzaklığı - Oyuncu 1 18-23, Oyuncu 2 0-5 kontrol edilir.       
        if (playerId == 1) {
            for (int i = 18; i < 24; i++) {// Siyah ev dolaşımı
                if (board[i][1] == playerId && board[i][0] > 0) { // Siyah taş varsa
                    max = Math.max(max, 24 - i);// Taşın çıkışa uzaklığı(Büyük olan tutulur)
                }
            }
        } else {// Beyaz ev dolaşımı
            for (int i = 5; i >= 0; i--) {
                if (board[i][1] == playerId && board[i][0] > 0) {
                    max = Math.max(max, i + 1);
                }
            }
        }
        return max;
    }

    public boolean hasAnyValidMove(int playerId) {// Hamle Geçerliliği
        if (bar[playerId] > 0) {// Bar da kırık taş var mı?
            
            int[] hedefler = new int[]{// Zara göre bar dan taşın gideceği yerler hesaplanır
                (playerId == 1) ? dice1 - 1 : 24 - dice1,// Siyah için zar-1
                (playerId == 1) ? dice2 - 1 : 24 - dice2 // Beyaz için 24-zar
            };

            for (int to : hedefler) { // Hedef tahta && (1 Rakip taş || Boşsa || Kendi Taşıysa) = En az 1 geçerli hamle
                if (to >= 0 && to < 24) {
                    if (board[to][0] <= 1 || board[to][1] == playerId) return true;
                }
            }
            return false;
        }

        for (int from = 0; from < 24; from++) {// Bütün tahtayı kontrol eder üsttekinin bar vaziyetinin dışında
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

    public boolean bothDiceUsed() { // Zarlar kullanıldı mı?
        return zar1Used && zar2Used && (!doubleRoll || (zar3Used && zar4Used));
    }

    public String serializeBoard() { // Tahta Bar durumu -> Taş sayısı, oyuncu numarası
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 24; i++) {
            sb.append(board[i][0]).append(",").append(board[i][1]);
            if (i < 23) sb.append(";");
        }
        sb.append("|BAR:").append(bar[1]).append(",").append(bar[2]);
        return sb.toString();
    }

    public void switchPlayer() {    //Oyuncu Değiştirme
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
    }

    public int getCurrentPlayer() { // Oyuncu numarası döndürür
        return currentPlayer;
    }

    public int getDice1() { // Zarları döndürür
        return dice1;
    }

    public int getDice2() {
        return dice2;
    }

    public boolean isGameOver() { // Oyun bitti mi?
        return out[1] == 15 || out[2] == 15;
    }

    public int getWinner() {    // Kazanan oyuncu numarasını döndürür
        return (out[1] == 15) ? 1 : 2;
    }

    public void printBoard() {  // Tahtayı konsola yazdırır
        System.out.println("----- Tahta Durumu -----");
        for (int i = 0; i < 24; i++) {
            if (board[i][0] > 0) {
                System.out.println("Hazne " + i + ": " + board[i][0] + " taş (Oyuncu " + board[i][1] + ")");
            }
        }
        System.out.println("-------------------------");
    }
}
