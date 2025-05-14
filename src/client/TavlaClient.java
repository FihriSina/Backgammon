// client/TavlaClient.java
package client;

import java.io.*;
import java.net.*;

public class TavlaClient {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 5000); // sunucuya bağlan
        System.out.println("Sunucuya bağlanıldı.");

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // sunucudan veri almak için
        String msg = in.readLine();
        System.out.println("Sunucudan gelen: " + msg);

        socket.close();
    }
}
