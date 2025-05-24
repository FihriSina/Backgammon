# Ağ Tabanlı Tavla Oyunu – Java

##  Proje Amacı

Bu proje, geleneksel **Tavla (Backgammon)** oyununun iki oyuncu tarafından **internet üzerinden gerçek zamanlı olarak oynanabilmesini** sağlayan, **Java tabanlı** bir ağ oyunudur. Proje sayesinde kullanıcılar fiziksel olarak aynı ortamda bulunmasalar bile bir sunucuya bağlanarak karşılıklı tavla oynayabilir.

##  Özellikler

-  **Gerçek zamanlı** istemci-sunucu iletişimi (Java Sockets)
-  **Zar atma** mekanizması (rastgele sayı üretimi)
-  **Oyun kurallarına tam uygunluk** (taş toplama, vurma, bar kontrolü, hamle sırası vs.)
-  **İki oyuncu destekli** yapı
-  **Java Swing ile kullanıcı dostu grafik arayüz**
-  Görsel taş ve tahta kullanımı: `.png` görselleriyle gerçekçi deneyim
-  **Bulut sunucuda çalışma desteği** (Google Cloud, SSH)
-  Gelişmiş hata kontrolü ve geçersiz hamle reddi
-  Çoklu iş parçacığı ile her oyuncu için ayrı sunucu işlemleri

##  Proje Dosya Yapısı

```

AbdullahSinaKORKMAZ_2021221044_networklab_2025_proje/
├── Tavla_Projesi/
│   ├── Client.java        # İstemci uygulaması (oyuncu arayüzü)
│   ├── Server.java        # Sunucu uygulaması (oyun yönetimi)
│   └── Game.java          # Oyun mantığı ve kurallar
└── res/
├── tavlatahtasi.png     # Oyun tahtası görseli
├── black_stone.png      # Siyah taş görseli
└── white_stone.png      # Beyaz taş görseli

````

## 🔧 Kurulum ve Çalıştırma

### 1. Java Kurulumu

Java 8 veya üzeri sürüm yüklü olmalıdır.

```bash
java -version
````

### 2. Projeyi Derleyin

```bash
cd src
javac *.java
```

### 3. Sunucuyu Başlatın

```bash
java Server
```

Sunucu hazır olduğunda "Bağlantı bekleniyor..." mesajı verecektir.

### 4. İstemcileri Başlatın

Farklı iki terminalde:

```bash
java Client
```

> IP ve port bilgileri `Client.java` içinde manuel ayarlanabilir. Sunucu aynı cihazda çalışıyorsa `localhost` yeterlidir.

##  Oynanış

1. İki oyuncu sunucuya bağlandığında oyun başlar.
2. Oyuncular sırayla zar atar ve taşlarını hareket ettirir.
3. Gerçek tavla kurallarına göre vurma, bar’dan giriş, taş toplama işlemleri desteklenir.
4. Tüm hamleler sunucu tarafından doğrulanır ve senkronize edilir.
5. Bir oyuncu tüm taşlarını topladığında oyun sona erer, kazanan ilan edilir.

##  Oyun Kuralları (Uygulanan)

* **15 taş ile oynanır**, klasik tavla dizilimi uygulanır.
* **Zar (2 adet, 1–6)** ile oynanır, çift gelirse 4 hamle yapılır.
* **Vurma (tek taşın bulunduğu nokta)** mümkündür, taş "bar" bölgesine alınır.
* **Bar'dan çıkmadan başka taş oynanamaz**.
* **Kapalı nokta (2+ taş)** rakibe karşı korumalıdır.
* **Taş toplama** yalnızca tüm taşlar iç sahadaysa mümkündür.
* **Oyun bitimi:** İlk tüm taşlarını toplayan kazanır.

##  Sunucu Kurulumu (Opsiyonel – Google Cloud)

* Sunucuya `SSH` ile bağlanın.
* Java kurulu değilse yükleyin.
* Proje dosyalarını aktarın.
* Aşağıdaki komutla başlatın:

```bash
java Server
```

> Uygun **port (örneğin 12345)** açılmalı ve IP adresi istemcilere bildirilmeli.

##  Geliştirme Notları

* **Oyun mantığı** Game.java içinde soyutlanmıştır.
* Tüm oyun durumu, sunucu tarafından yönetilir (otoriter yapı).
* Taş hareketleri arayüzde grafiksel olarak gösterilir.
* Kod, kolayca geliştirilip genişletilebilir:

  * Yapay zekalı rakip (AI)
  * Skor sistemi
  * Zaman sınırlı hamleler
  * Katlama küpü

## 📸 Örnek Arayüz

> Oyun başlangıcında kullanıcıya tahta, zarlar ve taşlar gösterilir.

![Tavla Tahtası](images/tavlatahtasi.png)

##  Kullanılan Teknolojiler

* Java SE
* Java Swing
* Socket Programlama (TCP)
* Çoklu İş Parçacığı (Threading)
* Google Cloud (opsiyonel)
* SSH Terminal Erişimi

##  Bilinen Sorunlar

* Aynı anda bağlantı denemeleri için ekstra kontrol eklenmelidir.
* Oyun bittikten sonra istemcilerin sıfırlanması manuel yapılmalı.
* Kullanıcı adları veya skor takibi henüz eklenmedi.

##  Katkı ve Geliştirme

Projeye katkıda bulunmak isteyenler, fork yaparak önerilerini uygulayabilir. Geri bildirim ve PR’lar memnuniyetle değerlendirilir.


# Network-Based Backgammon Game – Java

##  Project Objective

This project implements the classic **Backgammon game**, allowing two players to play in **real-time over a network** using a **Java-based client-server architecture**. Players can join from different machines and enjoy a complete backgammon experience online.

##  Features

-  **Real-time** communication via Java Sockets
-  **Dice roll mechanics** with random number generation
-  **Full game rules implementation** (bear off, hit, bar control, turn logic, etc.)
-  Supports **2 simultaneous players**
-  **User-friendly GUI** with Java Swing
-  Graphical board and stone visuals (`.png` format)
-  Compatible with **cloud environments** (e.g., Google Cloud SSH access)
-  Valid move checks and invalid move prevention
-  Multi-threaded server for handling each player individually

##  Project Structure

```

AbdullahSinaKORKMAZ_2021221044_networklab_2025_proje/
├── Tavla_Projesi/
│   ├── Client.java        # Client application (player UI)
│   ├── Server.java        # Server application (game logic handler)
│   └── Game.java          # Core game logic
└── res/
├── tavlatahtasi.png     # Game board image
├── black_stone.png      # Black stone image
└── white_stone.png      # White stone image

````

##  Setup & Execution

### 1. Java Setup

Ensure Java 8 or higher is installed.

```bash
java -version
````

### 2. Compile the Project

```bash
cd src
javac *.java
```

### 3. Run the Server

```bash
java Server
```

The server will start and wait for player connections on port `5000`.

### 4. Run Clients

In two separate terminals:

```bash
java Client
```

> IP and port can be configured in `Client.java`. Use `localhost` if running on the same machine as the server.

##  Gameplay

1. Game starts when two players connect to the server.
2. Players roll dice and take turns.
3. Stones can be moved, hit, and collected according to official rules.
4. All actions are validated and synchronized by the server.
5. The player who collects all stones first wins.

##  Implemented Game Rules

* **15 stones per player**, classic initial setup
* **Two dice (1–6)**, doubles allow 4 moves
* **Hit** mechanic: opponent's single stones are sent to the **bar**
* **Bar rule**: stones must be re-entered before any other move
* **Blocked points** (2+ stones) are protected
* **Bear off**: only when all stones are in the home area
* **Game end**: first player to collect all 15 stones wins

##  Server Deployment (Optional – Google Cloud)

* Connect to your Google Cloud VM via **SSH**
* Ensure Java is installed
* Upload your project files
* Run:

```bash
java Server
```

> Make sure the port (e.g., `5000`) is open, and update the client IP accordingly. You may change this IP later — **Google Cloud SSH** will be used for final deployment.

##  Development Notes

* Game mechanics are encapsulated in `Game.java`
* Server acts as an authoritative game state manager
* GUI updates dynamically reflect the current board state
* Code is modular and ready for future enhancements:

  * AI opponent
  * Score tracking
  * Timed turns
  * Doubling cube

##  Sample UI

> Visual interface includes the board, dice, and movable stones.

![Game Board](images/tavlatahtasi.png)

##  Technologies Used

* Java SE
* Java Swing (GUI)
* TCP Socket Programming
* Multi-threading (ExecutorService)
* Cloud compatibility (Google Cloud SSH)

##  Known Limitations

* No concurrency control for simultaneous new connections
* Game reset is manual post-completion
* No user login or persistent player tracking yet

##  Contributions & Extensions

Feel free to fork the project, submit pull requests, or open issues to suggest improvements. Contributions are welcome!

---


