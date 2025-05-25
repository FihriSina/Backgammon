package Tavla_Projesi;

// Gerekli Kütüphaneler
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Client extends JFrame { // GUI - Taş Seçip Hamle Yapma - Zar Atma - Mesaj
                                     // Sunucudan gelen mesaja göre GUI günceller
    private JPanel[] boardPanels = new JPanel[24];  // 24 Hazne için 24 JPanel
    private int selectedPoint = -1;                 // Oyuncunun seçtiği taşın bulunduğu hazne

    private JTextField userInputField;              // Kullanıcı mesaj girişi 
    private JTextArea chatArea;                     // Sohbet alanı, mesajları gösterir
    private JButton sendButton;                     // Mesaj gönderme butonu

    private JLabel diceLabel1, diceLabel2;          // Zar 1 ve Zar 2 için label
    private JButton rollDiceButton;                 // Zar atma butonu
    private JPanel boardPanel;                      // Tavla tahtası paneli
    private int playerId = -1;
    private boolean myTurn = false;                 // Oyuncunun sırası mı?

    private Socket socket;                          // Sunucu ile bağlantı için socket
    private BufferedReader input;                   // Sunucudan gelen mesajları okumak için BufferedReader (byte akışı)
    private PrintWriter output;                     // Sunucuya mesaj göndermek için PrintWriter (karakter akışı)

    private int zar1 = -1;                          // Zar 1 değeri
    private int zar2 = -1;                          // Zar 2 değeri

    private int[] bar = new int[3];                 // Bar'daki taş sayısı, bar[1] siyah, bar[2] beyaz için
    private JLabel barLabel;                        // Bar alanını gösteren label

    private ImageIcon blackStoneIcon;
    private ImageIcon whiteStoneIcon;

    private JButton barSelectButton;                // Bar'dan taş seçme butonu
    private boolean barSelected = false;            // Bar'dan taş seçildi mi?

    private JPanel barAreaPanel;                    // Bar alanı için panel, taşların gösterileceği yer 


//-----------------------------------------------GUI----------------------------------------------------------------
    private ImageIcon loadIcon(String path) {       // İkon Yükleme Metodu
        try {
            Image img = new ImageIcon(getClass().getResource(path)).getImage();
            Image scaled = img.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            System.err.println("İkon yüklenemedi: " + path);
            return null;
        }
    }

    private JPanel createPointPanel(int index) {    // Her bir hazne için JPanel oluşturma metodu
        JPanel pointPanel = new JPanel();// Panel oluştur
        pointPanel.setLayout(new BoxLayout(pointPanel, BoxLayout.Y_AXIS));//Taşlar üst üste gelsin -> layout
        pointPanel.setOpaque(false);// Paneli şeffaf yapılır
        pointPanel.setPreferredSize(new Dimension(50, 120));//Panel boyutu
        
        // Panel Üzerine gelince mavi kenarlık 
        pointPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                pointPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2)); // Hover olduğunda mavi kenarlık
            }
        
            @Override
            public void mouseExited(MouseEvent e) {
                pointPanel.setBorder(null); // Çıkınca border'ı kaldır
            }
        
            @Override
            public void mouseClicked(MouseEvent e) {// handlePointClick ile işlemleri yaparız
                handlePointClick(index);
            }
        });
    
        return pointPanel;
    }

    public Client(String serverIP, int serverPort) {
        setTitle("Tavla Client");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        blackStoneIcon = loadIcon("/res/black_stone.png");
        whiteStoneIcon = loadIcon("/res/white_stone.png");

        JPanel bottomPanel = new JPanel(new BorderLayout());//Alt kısım paneli
        userInputField = new JTextField();
        sendButton = new JButton("Gönder");
        barSelectButton = new JButton("Bar'dan Taş Seç");
        barSelectButton.setEnabled(false);

        barSelectButton.addActionListener(e -> {// Bar dan Seçme Butonu 
            if (bar[playerId] > 0 && myTurn) {
                barSelected = true;
                chatArea.append("Bar'daki taşı çıkarmak için hedef noktaya tıklayın.\n");
            }
        });

        // Konum ayarı
        bottomPanel.add(userInputField, BorderLayout.CENTER); // Kullanıcı girişi alanı - gönderme butonu - bar seçme butonu
        bottomPanel.add(sendButton, BorderLayout.EAST);
        bottomPanel.add(barSelectButton, BorderLayout.WEST);
        add(bottomPanel, BorderLayout.SOUTH);

        JPanel dicePanel = new JPanel();                 // Zar Panel oluştur
        diceLabel1 = new JLabel("Zar 1: -");        // Zarlar için label
        diceLabel2 = new JLabel("Zar 2: -");
        rollDiceButton = new JButton("Zar At");
        JButton resetButton = new JButton("Reset");
        dicePanel.add(resetButton);
        dicePanel.add(diceLabel1);
        dicePanel.add(diceLabel2);
        dicePanel.add(rollDiceButton);
        rollDiceButton.setEnabled(false);
        add(dicePanel, BorderLayout.NORTH);

        barLabel = new JLabel("Bar: -"); // Bar taşlarını gösteren label
        barLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        barLabel.setForeground(Color.RED);
        barLabel.setBorder(BorderFactory.createTitledBorder("Bar Alanı"));
        add(barLabel, BorderLayout.WEST);

        Image bg = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/res/tavlatahtasi.png"));
        boardPanel = new JPanel(null) {  // null layout -> Elle ayarlama
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            }
        };

        // Bar’daki taşların gösterileceği özel bir panel
        barAreaPanel = new JPanel();
        barAreaPanel.setLayout(new BoxLayout(barAreaPanel, BoxLayout.Y_AXIS));// Dikey şekilde
        barAreaPanel.setOpaque(false);  // Şeffaf arka plan
        barAreaPanel.setBounds(310, 220, 60, 120); // Tahtanın tam ortasına konumlandırılış
        boardPanel.add(barAreaPanel);


        // Her bir hazne için panel boyutları
        int panelWidth = 50;
        int panelHeight = 180;

        // Şimdi panelleri ekle ve konumlandır
        // X ve Y koordinatlarını -> manuel ayar

        int[] xPositions = new int[24];  // her hazne için x pozisyonu 
        int[] yPositions = new int[24];  // her hazne için y pozisyonu 

        // Üst satır: (23-12) sağdan sola, y = 10
        for (int i = 23, pos = 0; i >= 12; i--, pos++) {
            xPositions[i] = 10 + pos * (panelWidth + 5);
            yPositions[i] = 10;
        }

        // Alt satır: (0-11) soldan sağa, y = panelHeight + 25
        for (int i = 0; i <= 11; i++) {
            xPositions[i] = 10 + i * (panelWidth + 5);
            yPositions[i] = panelHeight + 25;
        }
        xPositions[0] = 25;  // Hazne 0 biraz daha sağa kayar
        yPositions[0] = 390; // Hazne 0 biraz daha aşağı kayar
        xPositions[1] = 75;  // Hazne 1 biraz daha sağa kayar
        yPositions[1] = 390; // Hazne 1 biraz daha aşağı kayar
        xPositions[2] = 125; // Hazne 2 biraz daha sağa kayar
        yPositions[2] = 390; // Hazne 2 biraz daha aşağı kayar
        xPositions[3] = 175; // Hazne 3 biraz daha sağa kayar
        yPositions[3] = 390; // Hazne 3 biraz daha aşağı kayar
        xPositions[4] = 225; // Hazne 4 biraz daha sağa kayar
        yPositions[4] = 390; // Hazne 4 biraz daha aşağı kayar
        xPositions[5] = 275; // Hazne 5 biraz daha sağa kayar
        yPositions[5] = 390; // Hazne 5 biraz daha aşağı kayar

        xPositions[6] = 350; // Hazne 6 biraz daha sağa kayar
        yPositions[6] = 390; // Hazne 6 biraz daha aşağı kayar
        xPositions[7] = 400; // Hazne 7 biraz daha sağa kayar
        yPositions[7] = 390; // Hazne 7 biraz daha aşağı kayar
        xPositions[8] = 450; // Hazne 8 biraz daha sağa kayar
        yPositions[8] = 390; // Hazne 8 biraz daha aşağı kayar
        xPositions[9] = 500; // Hazne 9 biraz daha sağa kayar
        yPositions[9] = 390; // Hazne 9 biraz daha aşağı kayar
        xPositions[10] = 550; // Hazne 10 biraz daha sağa kayar
        yPositions[10] = 390; // Hazne 10 biraz daha aşağı kayar
        xPositions[11] = 600; // Hazne 11 biraz daha sağa kayar
        yPositions[11] = 390; // Hazne 11 biraz daha aşağı kayar

        xPositions[12] = 600; // Hazne 12 biraz daha sağa kayar
        yPositions[12] = 30;  // Hazne 12 biraz daha aşağı kayar
        xPositions[13] = 550; // Hazne 13 biraz daha sağa kayar
        yPositions[13] = 30;  // Hazne 13 biraz daha aşağı kayar
        xPositions[14] = 500; // Hazne 14 biraz daha sağa kayar
        yPositions[14] = 30;  // Hazne 14 biraz daha aşağı kayar
        xPositions[15] = 450; // Hazne 15 biraz daha sağa kayar
        yPositions[15] = 30;  // Hazne 15 biraz daha aşağı kayar
        xPositions[16] = 400; // Hazne 16 biraz daha sağa kayar
        yPositions[16] = 30;  // Hazne 16 biraz daha aşağı kayar
        xPositions[17] = 350; // Hazne 17 biraz daha sağa kayar
        yPositions[17] = 30;  // Hazne 17 biraz daha aşağı kayar

        xPositions[18] = 275;  // Hazne 18 biraz daha sağa kayar
        yPositions[18] = 30;   // Hazne 18 biraz daha aşağı kayar
        xPositions[19] = 225;  // Hazne 19 biraz daha sağa kayar
        yPositions[19] = 30;   // Hazne 19 biraz daha aşağı kayar
        xPositions[20] = 175;  // Hazne 20 biraz daha sağa kayar
        yPositions[20] = 30;   // Hazne 20 biraz daha aşağı kayar
        xPositions[21] = 125;  // Hazne 21 biraz daha sağa kayar
        yPositions[21] = 30;   // Hazne 21 biraz daha aşağı kayar
        xPositions[22] = 75;   // Hazne 22 biraz daha sağa kayar
        yPositions[22] = 30;   // Hazne 22 biraz daha aşağı kayar
        xPositions[23] = 25;   // Hazne 23 biraz daha sağa kayar
        yPositions[23] = 30;   // Hazne 23 biraz daha aşağı kayar

        // Panelleri oluştur, konumlandır ve ekle (24 Hazne)
        for (int i = 0; i < 24; i++) {
            JPanel pointPanel = createPointPanel(i);
            pointPanel.setBounds(xPositions[i], yPositions[i], panelWidth, panelHeight);
            boardPanel.add(pointPanel);
            boardPanels[i] = pointPanel;
        }

        // Tahta panelinin toplam boyutu
        boardPanel.setPreferredSize(new Dimension(
            (panelWidth + 5) * 12 + 20,  // genişlik (12 kolon)
            panelHeight * 2 + 35          // yükseklik (2 satır + aralar)
        ));

        add(boardPanel, BorderLayout.EAST);// Tahta paneli sağda olacak

        resetButton.addActionListener(e -> output.println("reset_game"));// Reset mesajı gönderme
        rollDiceButton.addActionListener(e -> {
            if (myTurn) output.println("roll_dice");// zar atma mesajı
        });

        sendButton.addActionListener(e -> sendMessage()); // Enter tuşu veya butona basış
        userInputField.addActionListener(e -> sendMessage());
//-----------------------------------------------GUI----------------------------------------------------------------
        try {// Sunucu bağlantı
            socket = new Socket(serverIP, serverPort);// Sunucu IP ve portuna bağla
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));// Sunucudan gelen mesajları okumak için BufferedReader
            output = new PrintWriter(socket.getOutputStream(), true);// mesaj gönderme - tabi sunucuya
            chatArea.append("Server'a bağlandı: " + serverIP + "\n");// Bağlantı başarılı

            new Thread(() -> {// sunucu mesaşlarını dinleme için thread
                try {
                    String msg;
                    while ((msg = input.readLine()) != null) {// Sürekli mesaj okuma
                        if (msg.startsWith("Sunucuya bağlandınız.")) {// Oyuncu ID al - yazdır
                            playerId = Integer.parseInt(msg.replaceAll("[^0-9]", ""));
                            chatArea.append("Oyuncu ID: " + playerId + "\n");
                            continue;
                        }
                        if (msg.equals("SIRA")) {// Sıra gelir zar atma butonu aktif
                            myTurn = true;
                            rollDiceButton.setEnabled(true);
                            chatArea.append("Sıra sizde! Zar atabilirsiniz.\n");
                            continue;
                        }
                        if (msg.startsWith("OYUN_BITTI:")) {//Oyun bitti zar atma butonu iptal 
                            chatArea.append("🎉 " + msg + "\n");
                            rollDiceButton.setEnabled(false);
                            myTurn = false;
                            continue; 
                        }
                        if (msg.startsWith("TAHTA:")) {// Tahta güncelle
                            updateBoardFromString(msg.substring(6));
                            continue;
                        }
                        if (msg.startsWith("ZARLAR:")) {// Zar değerleri güncelle
                            String[] z = msg.substring(7).split(",");
                            zar1 = Integer.parseInt(z[0]);
                            zar2 = Integer.parseInt(z[1]);
                            diceLabel1.setText("Zar 1: " + zar1);
                            diceLabel2.setText("Zar 2: " + zar2);
                            rollDiceButton.setEnabled(false);
                            chatArea.append("Zarlarınız geldi: " + zar1 + " ve " + zar2 + "\n");
                            continue;
                        }
                        if (msg.equals("RESET")) {// Sıfırlama ile zar ve seçimler gider
                            diceLabel1.setText("Zar 1: -");
                            diceLabel2.setText("Zar 2: -");
                            zar1 = zar2 = -1;
                            selectedPoint = -1;
                            chatArea.append("🔁 Oyun sıfırlandı. Zarlar temizlendi.\n");
                            continue;
                        }
                        if (msg.startsWith("RAKIP_ZAR:")) {// Rakip zarı gösterme
                            String[] r = msg.substring(10).split(",");
                            diceLabel1.setText("Rakip Zar 1: " + r[0]);
                            diceLabel2.setText("Rakip Zar 2: " + r[1]);
                            myTurn = false;
                            rollDiceButton.setEnabled(false);
                            chatArea.append("Rakip zar attı: " + r[0] + " ve " + r[1] + "\n");
                            continue;
                        }
                        chatArea.append("Server: " + msg + "\n");
                    }
                } catch (IOException ex) {
                    chatArea.append("Bağlantı kesildi.\n");
                }
            }).start();
        } catch (IOException e) {
            chatArea.append("Server bağlantı hatası: " + e.getMessage() + "\n");
        }
    }

    private void updateBoardFromString(String data) {
        if (playerId == -1) return; // Oyuncu ID mecburiyeti
        String[] points = data.split(";"); // 24 nokta için veriler
        String barInfo = "";// Bar bilgisi başlangıçta boş
        if (data.contains("|BAR:")) {// Bar bilgisi varsa ayır
            String[] split = data.split("\\|BAR:");
            data = split[0];
            barInfo = split[1];
            points = data.split(";");// Points sadece 24 hazne bilgisini(Taş sayısı ve sahibi) tutar
        }

        for (int i = 0; i < 24; i++) {// Her bir hazne için 
            String[] p = points[i].split(","); // Taş sayısı ve sahibi bilgisi
            int count = Integer.parseInt(p[0]); // Taş sayısı
            int owner = Integer.parseInt(p[1]); // Taş sahibi (1 siyah, 2 beyaz)
            boardPanels[i].removeAll(); // Önceki taşları temizle

            for (int j = 0; j < count; j++) {// Taş sayısı kadar JLabel ekle (İkon için)
                JLabel lbl = new JLabel();// JLabel oluştur
                lbl.setAlignmentX(Component.CENTER_ALIGNMENT);// Ortalanmış hizalama
                lbl.setPreferredSize(new Dimension(30, 30));// Taş boyutu
                ImageIcon baseIcon = (owner == 1) ? blackStoneIcon : whiteStoneIcon;// siyah / beyaz
                if (baseIcon != null) {// İkon varsa - ölçeklendir ve ata
                    Image scaledImg = baseIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH); 
                    lbl.setIcon(new ImageIcon(scaledImg));
                }
                lbl.putClientProperty("owner", owner); // Taş sahibi bilgisini sakla
                boardPanels[i].add(lbl); // JLabel'i panel'e ekle
            }
            boardPanels[i].revalidate();// Paneli güncelle
            boardPanels[i].repaint();
        }

        if (!barInfo.isEmpty()) {// Bar bilgisi varsa
            String ownSymbol = (playerId == 1) ? "⚫" : "⚪";
            String[] barParts = barInfo.split(",");// Bar bilgisi 
            bar[1] = Integer.parseInt(barParts[0]);// Bar'daki siyah taş sayısı
            bar[2] = Integer.parseInt(barParts[1]);//          Beyaz
            StringBuilder barStr = new StringBuilder("Bar: ");// Bar label'ı için metin
            for (int i = 0; i < bar[playerId]; i++) barStr.append(ownSymbol);// Bar'daki taş sayısı kadar sembol ekle
            barLabel.setText(barStr.toString());// Bar label'ını güncelle
            barSelectButton.setEnabled(bar[playerId] > 0 && myTurn);// Bar'dan taş seçme butonu aktif mi?
            if (bar[playerId] > 0)// Eğer bar'da taş varsa
                chatArea.append("Bar'da taşınız var. Önce onu tahtaya koymalısınız.\n");
        
            // BAR panelini sıfırla ve taşları güncelle
            barAreaPanel.removeAll();
            for (int i = 0; i < bar[playerId]; i++) {// Bar'daki taş sayısı kadar JLabel ekle
                JLabel barStone = new JLabel();// JLabel oluştur
                barStone.setAlignmentX(Component.CENTER_ALIGNMENT);//
                barStone.setPreferredSize(new Dimension(30, 30));//
                ImageIcon baseIcon = (playerId == 1) ? blackStoneIcon : whiteStoneIcon;
                if (baseIcon != null) {
                    Image scaledImg = baseIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                    barStone.setIcon(new ImageIcon(scaledImg));
                }
                barAreaPanel.add(barStone);
            }
            barAreaPanel.revalidate();
            barAreaPanel.repaint();
        }
    }        

    // Taş seçme ve hamle yapma işlemleri - Kısaca hazneye tıkladığında ne olacağını yöneten metot
    private void handlePointClick(int pointIndex) {
        if (!myTurn) {// Eğer oyuncunun sırası değilse
            chatArea.append("Sıra sizde değil.\n");
            return;
        }

        if (bar[playerId] > 0 && !barSelected) {// Eğer bar'da taş varsa ve henüz bar seçilmemişse
            chatArea.append("Bar'daki taşı çıkarmalısınız. Önce Bar butonuna tıklayın.\n");
            return;
        }

        if (barSelected) {// Eğer bar'dan taş seçilmişse gideceği yerler hesap
            int h1 = (playerId == 1) ? zar1 - 1 : 24 - zar1;
            int h2 = (playerId == 1) ? zar2 - 1 : 24 - zar2;

            if (pointIndex != h1 && pointIndex != h2) {
                chatArea.append("Bar'daki taşı sadece " + h1 + " veya " + h2 + ". noktaya koyabilirsiniz.\n");
                return;
            }

            output.println("move:-1->" + pointIndex);
            chatArea.append("Bar'dan hamle: -> " + pointIndex + "\n");
            barSelected = false;
            return;
        }

        int count = boardPanels[pointIndex].getComponentCount();// Seçilen haznedeki taş sayısı
        if (selectedPoint == -1) {// Haznede taş yoksa
            if (count == 0) {
                chatArea.append("Taş yok.\n");
                return;
            }
            JLabel topStone = (JLabel) boardPanels[pointIndex].getComponent(count - 1);
            Integer owner = (Integer) topStone.getClientProperty("owner");
            if (owner == null || owner != playerId) {
                chatArea.append("Bu taş size ait değil.\n");
                return;
            }
            selectedPoint = pointIndex;
            boardPanels[selectedPoint].setBackground(Color.YELLOW); // Vurgulu seçim
            chatArea.append("Taş seçildi: " + pointIndex + "\n");              
        } else {// Zar sayısı geldi ve çıkabilir out mesajı sunucuya gider
            int h1 = (playerId == 1) ? selectedPoint + zar1 : selectedPoint - zar1;
            int h2 = (playerId == 1) ? selectedPoint + zar2 : selectedPoint - zar2;

            if ((playerId == 1 && (selectedPoint + zar1 == 24 || selectedPoint + zar2 == 24)) ||
                (playerId == 2 && (selectedPoint - zar1 == -1 || selectedPoint - zar2 == -1))) {
                output.println("move:" + selectedPoint + "->OUT");
                chatArea.append("Taş dışarı çıkarıldı.\n");
                selectedPoint = -1;
                return;
            }

            if (pointIndex != h1 && pointIndex != h2) {
                chatArea.append("Zara uygun nokta değil.\n");
                selectedPoint = -1;
                return;
            }

            output.println("move:" + selectedPoint + "->" + pointIndex);
            chatArea.append("Hamle: " + selectedPoint + " -> " + pointIndex + "\n");
            boardPanels[selectedPoint].setBackground(null); // Arka planı temizle
            selectedPoint = -1;
        }
    }

    private void sendMessage() {// Kullanıcı mesaj gönderme metodu
        String msg = userInputField.getText().trim();
        if (!msg.isEmpty()) {
            output.println(msg);// sunucuya mesaj gönder
            chatArea.append("Sen: " + msg + "\n");
            userInputField.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {// GUI'yi başlat
            Client client = new Client("127.0.0.1", 5000);
            client.setVisible(true);
        });
    }
}
