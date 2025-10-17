package io.github.winnpixie.http4j.server.program;

import io.github.winnpixie.http4j.server.HttpServer;

import java.nio.file.Paths;

public class Http4JServerProgram {
    public static void main(String[] args) {
        HttpServer server = new HttpServer(8080);

        for (int i = 0; i < args.length; i++) {
            if (i + 1 == args.length) break;

            String arg = args[i];
            String next = args[i + 1];
            switch (arg.toLowerCase()) {
                case "--root":
                case "-r":
                    server.getRequestHandlers().getDefaultHandler().setRoot(Paths.get(next));
                    break;
                case "--port":
                case "-p":
                    server.setPort(Integer.parseInt(next));
                    break;
            }
        }

        server.start();
    }
}
