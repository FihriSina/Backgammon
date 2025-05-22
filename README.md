# AbdullahSinaKORKMAZ_2021221044_networklab_2025_proje
Bilgisayar Ağları Dersi Projesi – Tavla Oyunu

TavlaGameProject/
├── Tavla_Projesi/
│   ├── Client.java
│   ├── Server.java
│   ├── Game.java
│   └── res/
│       └── tavlatahtasi.png  ✅ BURAYA KOY


    2. Temel Bileşenler
🔸 Server.java

    Client bağlantılarını kabul eder (Socket)

    Her bağlantı için bir ClientHandler oluşturur

🔸 Game.java

    Oyun tahtasını, taşların pozisyonlarını, sıraları ve durumu saklar

    Taş hareketi gibi temel mantık burada işlenir

🔸 Client.java

    Server’a bağlanır, GUI’den gelen hareketleri gönderir

    Server’dan gelen mesajlara göre GUI’yi günceller

