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

TavlaOyunu/
├── src/
│   ├── Client.java      # İstemci uygulaması (oyuncu arayüzü)
│   ├── Server.java      # Sunucu uygulaması (oyun yönetimi)
│   └── Game.java        # Oyun mantığı ve kurallar
└── images/
├── tavlatahtasi.png     # Oyun tahtası görseli
├── black\_stone.png      # Siyah taş görseli
└── white\_stone.png      # Beyaz taş görseli

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

