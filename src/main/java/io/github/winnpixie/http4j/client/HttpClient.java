package io.github.winnpixie.http4j.client;

import io.github.winnpixie.http4j.shared.HttpMethod;
import io.github.winnpixie.http4j.shared.HttpStatus;
import io.github.winnpixie.http4j.shared.throwables.HttpException;
import io.github.winnpixie.http4j.shared.utilities.Constants;
import io.github.winnpixie.http4j.shared.utilities.IOHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpClient {
    private final ExecutorService threadedExecutor;

    private HttpClient(int threads) {
        this.threadedExecutor = Executors.newFixedThreadPool(threads);
    }

    public static HttpClient newClient() {
        return newClient(8);
    }

    public static HttpClient newClient(int threads) {
        return new HttpClient(threads);
    }

    public HttpRequest.Builder newRequest() {
        return new HttpRequest.Builder()
                .setHeader("User-Agent", Constants.CLIENT_ID);
    }

    public HttpResponse send(HttpRequest request) throws HttpException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) request.getUrl().openConnection(request.getProxy());
            connection.setRequestMethod(request.getMethod().getVerb());
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(request.isFollowRedirects());

            for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            if (request.getMethod().equals(HttpMethod.PUT) || request.getMethod().equals(HttpMethod.POST)) {
                connection.setRequestProperty("Content-Length", Integer.toString(request.getBody().length));

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(request.getBody());
                    os.flush();
                }
            }

            try (InputStream is = connection.getInputStream()) {
                return new HttpResponse.Builder()
                        .setRequest(request)
                        .setStatus(HttpStatus.from(connection.getResponseCode()))
                        .setHeaders(connection.getHeaderFields())
                        .setBody(IOHelper.toByteArray(is))
                        .build();
            }
        } catch (IOException ioe) {
            throw new HttpException("Error with request", ioe);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public CompletableFuture<HttpResponse> sendAsync(HttpRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return send(request);
            } catch (HttpException he) {
                throw new CompletionException(he);
            }
        }, threadedExecutor);
    }
}