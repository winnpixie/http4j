package io.github.winnpixie.httpsrv;

import java.io.File;

public class HttpServerMain {
    public static void main(String[] args) {
        int port = 8080;
        String path = "www";

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--port":
                case "-p":
                    if (i + 1 > args.length) return;

                    port = Integer.parseInt(args[i + 1]);
                    break;
                case "--root":
                    StringBuilder pathBuilder = new StringBuilder();
                    for (int i2 = 1; i2 < args.length; i2++) {
                        String nextArg = args[i + i2];
                        if (nextArg.equalsIgnoreCase("--port") || nextArg.equalsIgnoreCase("-p")) break;

                        pathBuilder.append(nextArg);
                    }

                    path = pathBuilder.toString();
                    break;
            }
        }

        HttpServer server = new HttpServer(port, new File(path));
        server.start();
    }
}
