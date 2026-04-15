package io.github.winnpixie.http4j.client;

import io.github.winnpixie.http4j.shared.HttpMethod;
import io.github.winnpixie.http4j.shared.HttpStatus;
import io.github.winnpixie.http4j.shared.utilities.Constants;
import io.github.winnpixie.http4j.shared.utilities.IOHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpClient {
    private final ExecutorService threadExecutor;

    private HttpClient(int threadLimit) {
        this.threadExecutor = Executors.newFixedThreadPool(threadLimit);
    }

    public static HttpClient newClient() {
        return newClient(8);
    }

    public static HttpClient newClient(int threadLimit) {
        return new HttpClient(threadLimit);
    }

    public Request.Builder newRequest() {
        return new Request.Builder()
                .setHeader("User-Agent", Constants.CLIENT_ID);
    }

    public Response send(Request request) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) request.getUrl().openConnection(request.getProxy());
            connection.setRequestMethod(request.getMethod().name());
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(request.isFollowRedirects());

            request.getHeaders().forEach(connection::setRequestProperty);

            if (request.getMethod().equals(HttpMethod.PUT) || request.getMethod().equals(HttpMethod.POST)) {
                connection.setRequestProperty("Content-Length", Integer.toString(request.getBody().length));

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(request.getBody());
                    os.flush();
                }
            }

            try (InputStream is = connection.getInputStream()) {
                return new Response.Builder()
                        .setRequest(request)
                        .setStatus(HttpStatus.from(connection.getResponseCode()))
                        .setHeaders(connection.getHeaderFields())
                        .setBody(IOHelper.toByteArray(is))
                        .build();
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public CompletableFuture<Response> sendAsync(Request request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return send(request);
            } catch (IOException exc) {
                throw new CompletionException(exc);
            }
        }, threadExecutor);
    }
}