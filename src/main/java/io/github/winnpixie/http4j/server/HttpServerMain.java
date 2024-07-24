package io.github.winnpixie.http4j.server;

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
                    i++;
                    break;
                case "--root":
                    StringBuilder pathBuilder = new StringBuilder();
                    for (i++; i < args.length; i++) {
                        String nextArg = args[i];
                        if (nextArg.indexOf('-') == 0) {
                            i--;
                            break;
                        }

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
