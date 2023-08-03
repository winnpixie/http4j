package io.github.winnpixie.webserver;

import java.io.File;

public class WebServerMain {
    public static void main(String[] args) {
        var server = new HttpServer(80, new File("www"));
        server.start();
    }
}
