package io.github.winnpixie.http4j.client;

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
        request.getMethod().getSendFunction().accept(request);
    }
}