package io.github.winnpixie.http4j.program;

import io.github.winnpixie.http4j.client.HttpClient;
import io.github.winnpixie.http4j.server.HttpServer;
import io.github.winnpixie.http4j.server.handlers.impl.StaticPathHandler;
import io.github.winnpixie.http4j.shared.throwables.HttpException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Http4JDemo {
    private static final Logger logger = Logger.getLogger(Http4JDemo.class.getName());

    public static void main(String[] args) {
        // # DEFAULTS
        String url = "https://checkip.amazonaws.com/";

        int port = 8080;
        Path path = Paths.get(".");

        // # ARG PROCESSING
        boolean skip = false;
        for (int i = 0; i < args.length; i++) {
            if (i + 1 == args.length) {
                break;
            }

            if (skip) {
                skip = false;
                continue;
            }

            String key = args[i];
            String value = args[i + 1];
            switch (key.toLowerCase()) {
                case "--url":
                case "-u":
                    url = value;
                    skip = true;
                    break;
                case "--port":
                case "-p":
                    port = Integer.parseInt(value);
                    skip = true;
                    break;
                case "--root":
                case "-r":
                    path = Paths.get(value);
                    skip = true;
                    break;
                default:
                    logger.log(Level.INFO, "http4j [key]... [value]...");
                    logger.log(Level.INFO, "> CLIENT ARGS");
                    logger.log(Level.INFO, "> --url, -u URL");
                    logger.log(Level.INFO, "> SERVER ARGS");
                    logger.log(Level.INFO, "> --root, -r PATH");
                    logger.log(Level.INFO, "> --port, -p PORT");
                    break;
            }
        }

        // # RUNNERS
        logger.log(Level.INFO, "Client demo");
        runClient(url);

        logger.log(Level.INFO, "Server demo");
        runServer(port, path);
    }

    private static void runClient(String url) {
        try {
            HttpClient client = HttpClient.newClient(); // OPTIONAL: user-defined limit for async thread count

            client.sendAsync(client.newRequest()
                            .setUrl(url)
                            .build())
                    .whenComplete((response, throwable) -> {
                        if (throwable != null) {
                            logger.log(Level.WARNING, throwable, () -> "[client] Error processing request");
                            return;
                        }

                        logger.log(Level.INFO, () -> String.format("[client]%n%s", response.getBodyAsString()));
                    });
        } catch (HttpException he) {
            logger.log(Level.WARNING, he, () -> "[client] Error constructing request");
        }
    }

    private static void runServer(int port, Path root) {
        StaticPathHandler handler = new StaticPathHandler();
        handler.setRoot(root);

        HttpServer server = new HttpServer(port); // OPTIONAL: user-defined limit for socket back-log amount
        server.getPathHandlers().add(handler);

        server.start();
    }
}
