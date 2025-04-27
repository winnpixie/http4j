package io.github.winnpixie.http4j.client;

import io.github.winnpixie.http4j.shared.HttpMethod;
import io.github.winnpixie.http4j.shared.HttpStatus;
import io.github.winnpixie.http4j.shared.utilities.IOHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpClient {
    private Request request;

    private HttpClient() {
        newRequest();
    }

    public static HttpClient newClient() {
        return new HttpClient();
    }

    public HttpClient newRequest() {
        request = new Request();

        return addHeader("User-Agent", "winnpixie/http4j (client)");
    }

    public HttpClient setMethod(HttpMethod method) {
        request.setMethod(method);

        return this;
    }

    public HttpClient setMethod(String method) {
        return setMethod(HttpMethod.from(method));
    }

    public HttpClient setUrl(URL url) {
        request.setUrl(url);

        return this;
    }

    public HttpClient setUrl(URI uri) {
        try {
            return setUrl(uri.toURL());
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }


    public HttpClient setUrl(String url) {
        try {
            return setUrl(URI.create(url).toURL());
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

    public Response send() throws IOException {
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
                }
            }

            try (InputStream is = conn.getInputStream()) {
                return new Response(request, HttpStatus.fromCode(conn.getResponseCode()),
                        IOHelper.toByteArray(is), conn.getHeaderFields());
            }
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}