# AbdullahSinaKORKMAZ_2021221044_networklab_2025_proje
Bilgisayar Ağları Dersi Projesi – Tavla Oyunu

TavlaGameProject/
│
├── server/                 # Sunucu tarafı kodları
│   ├── Server.java         # Ana sunucu sınıfı (bağlantıları kabul eder)
│   ├── ClientHandler.java  # Her istemci için ayrı iş parçacığı (oyuncu işlemleri)
│   ├── GameState.java      # Oyun durumu, taşların konumu, sıra bilgisi
│   └── Message.java        # Sunucu-istemci iletişim protokolü
│
├── client/                 # İstemci tarafı kodları
│   ├── Client.java         # Ana istemci sınıfı (sunucuya bağlanır)
│   ├── GameGUI.java        # Swing tabanlı arayüz
│   └── Message.java        # Ortak mesaj nesnesi
│
├── shared/                 # Ortak bileşenler (opsiyonel)
│   └── Constants.java      # Mesaj tipleri, oyun kuralları, sabitler
│
└── README.md               # Proje açıklaması

    2. Temel Bileşenler
🔸 Server.java

    Client bağlantılarını kabul eder (Socket)

    Her bağlantı için bir ClientHandler oluşturur

🔸 ClientHandler.java

    Her istemciye özel çalışır (Runnable)

    Oyuncu hamlelerini işler, GameState üzerinden güncelleme yapar

    Diğer oyuncuya durumu gönderir

🔸 GameState.java

    Oyun tahtasını, taşların pozisyonlarını, sıraları ve durumu saklar

    Taş hareketi gibi temel mantık burada işlenir

🔸 Message.java

    Tip + içerik taşıyan mesaj objesi (örnek: MOVE, ROLL, JOIN, WIN, ...)

    Hem istemci hem sunucu aynı Message sınıfını kullanır (JSON veya Java Serializable olabilir)

🔸 Client.java

    Server’a bağlanır, GUI’den gelen hareketleri gönderir

    Server’dan gelen mesajlara göre GUI’yi günceller

🔸 GameGUI.java

    Tavla tahtasını Swing ile çizer

    Butonlar: "Zar At", "Taşı Seç", "Hamle Yap"

    Oyun sonunda kazananı gösterir


    [Client1] ←→ [Server] ←→ [Client2]

Bağlantı:
- Client bağlanır → Server kabul eder
- İki oyuncu bağlandıysa → "Oyun başlıyor" mesajı

Oyun:
- Client1 zar atar → hamle yapar → Server günceller → Client2’ye gönderir
- Client2 aynı şekilde devam eder

Oyun Bitti:
- Server kazanma durumunu kontrol eder → her iki istemciye bilgi verir
