package io.github.winnpixie.webserver;

import java.io.File;

public class WebServerMain {
    public static void main(String[] args) {
        new HttpServer(80, new File("www")).start();
    }
}
