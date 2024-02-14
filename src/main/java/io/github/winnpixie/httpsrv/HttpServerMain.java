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
                    for (int si = 1; si < args.length - i; si++) {
                        String nextArg = args[i + si];
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
