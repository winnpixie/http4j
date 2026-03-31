package io.github.winnpixie.http4j.client;

import io.github.winnpixie.http4j.shared.HttpMethod;
import io.github.winnpixie.http4j.shared.HttpStatus;
import io.github.winnpixie.http4j.shared.utilities.Constants;
import io.github.winnpixie.http4j.shared.utilities.IOHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.function.Consumer;

public class HttpClient {
    private HttpClient() {
    }

    public static HttpClient newClient() {
        return new HttpClient();
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

    public void send(Request request, Consumer<Response> onSuccess, Consumer<Exception> onError) {
        new Thread(() -> {
            try {
                Response response = send(request);
                onSuccess.accept(response);
            } catch (Exception exception) {
                onError.accept(exception);
            }
        }, "http4j_client_send").start();
    }
}