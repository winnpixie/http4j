package io.github.winnpixie.http4j.client;

import io.github.winnpixie.http4j.shared.HttpMethod;
import io.github.winnpixie.http4j.shared.HttpStatus;
import io.github.winnpixie.http4j.shared.utilities.IOHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;

public class HttpClient {
    private HttpRequest request;

    private HttpClient() {
        newRequest();
    }

    public static HttpClient newClient() {
        return new HttpClient();
    }

    public HttpClient newRequest() {
        request = new HttpRequest();

        return addHeader("User-Agent", "winnpixie/http4j (client)");
    }

    public HttpClient setMethod(HttpMethod method) {
        request.setMethod(method);

        return this;
    }

    public HttpClient setMethod(String methodVerb) {
        return setMethod(HttpMethod.from(methodVerb));
    }

    public HttpClient setUrl(URL url) {
        request.setUrl(url);

        return this;
    }

    public HttpClient setUrl(String url) {
        try {
            return setUrl(new URL(url));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpClient setHeaders(Map<String, String> headers) {
        request.setHeaders(headers);

        return this;
    }

    public HttpClient addHeader(String key, String value) {
        request.getHeaders().put(key, value);

        return this;
    }

    public HttpClient addHeaders(String[]... headers) {
        for (String[] header : headers) {
            addHeader(header[0], header[1]);
        }

        return this;
    }

    public HttpClient setProxy(Proxy proxy) {
        request.setProxy(proxy);

        return this;
    }

    public HttpClient setBody(byte[] body) {
        request.setBody(body);

        return this;
    }

    public HttpClient setBody(String body, Charset charset) {
        return setBody(body.getBytes(charset));
    }

    public HttpClient setBody(String body) {
        return setBody(body, StandardCharsets.UTF_8);
    }

    public HttpClient setFollowRedirects(boolean followRedirects) {
        request.setFollowRedirects(followRedirects);

        return this;
    }

    public HttpClient onSuccess(Consumer<HttpResponse> onSuccess) {
        request.setOnSuccess(onSuccess);

        return this;
    }

    public HttpClient onFailure(Consumer<Exception> onFailure) {
        request.setOnFailure(onFailure);

        return this;
    }

    public void send() {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) request.getUrl().openConnection(request.getProxy());
            conn.setRequestMethod(request.getMethod().name());
            conn.setInstanceFollowRedirects(request.isFollowRedirects());
            request.getHeaders().forEach(conn::setRequestProperty);

            if (request.getMethod().equals(HttpMethod.PUT) || request.getMethod().equals(HttpMethod.POST)) {
                conn.setRequestProperty("Content-Length", Integer.toString(request.getBody().length));

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(request.getBody());
                    os.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try (InputStream is = conn.getInputStream()) {
                request.getOnSuccess().accept(new HttpResponse(request, HttpStatus.fromCode(conn.getResponseCode()),
                        IOHelper.toByteArray(is), conn.getHeaderFields()));
            }
        } catch (Exception e) {
            request.getOnFailure().accept(e);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}