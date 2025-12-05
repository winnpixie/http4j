package io.github.winnpixie.http4j.program;

import io.github.winnpixie.http4j.client.HttpClient;
import io.github.winnpixie.http4j.client.Request;
import io.github.winnpixie.http4j.server.HttpServer;
import io.github.winnpixie.http4j.shared.HttpMethod;

import java.net.MalformedURLException;
import java.net.Proxy;
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
        for (int i = 0; i < args.length; i++) {
            if (i + 1 == args.length) break;

            String key = args[i];
            String value = args[i + 1];
            switch (key.toLowerCase()) {
                case "--url":
                case "-u":
                    url = value;
                    break;
                case "--port":
                case "-p":
                    port = Integer.parseInt(value);
                    break;
                case "--root":
                case "-r":
                    path = Paths.get(value);
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
            HttpClient client = HttpClient.newClient();
            Request request = client.newRequest().setUrl(url)
                    .setMethod(HttpMethod.GET)
                    .setProxy(Proxy.NO_PROXY)
                    .create();

            request.sendNonBlocking(
                    resp -> logger.log(Level.INFO, () -> String.format("[client]%n%s", resp.getBodyAsString())),
                    exc -> logger.log(Level.WARNING, exc, () -> "[client] Error processing request")
            );
        } catch (MalformedURLException mue) {
            logger.log(Level.WARNING, mue, () -> "[client] Malformed URL");
        }
    }

    private static void runServer(int port, Path root) {
        HttpServer server = new HttpServer(port);

        server.getRequestHandlers().getDefaultHandler().setRoot(root);
        server.start();
    }
}
