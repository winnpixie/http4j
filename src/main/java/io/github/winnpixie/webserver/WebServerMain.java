package io.github.winnpixie.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class WebServerMain {
    public static void main(String[] args) {
        var server = new Server(80, new File("www"));

        server.setRunning(true);
        server.startThread();

        try {
            Thread.sleep(5000);
            try (var socket = new Socket("localhost", 80)) {
                socket.getOutputStream().write("GET /../jws-0.0.1-SNAPSHOT.jar HTTP/1.1\n\n".getBytes(StandardCharsets.UTF_8));

                socket.getInputStream();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
