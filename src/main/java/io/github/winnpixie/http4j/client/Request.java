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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Request {
    private final HttpMethod method;
    private final URL url;
    private final Map<String, String> headers;
    private final Proxy proxy;
    private final byte[] body;
    private final boolean followRedirects;

    private Request(HttpMethod method,
                    URL url,
                    Map<String, String> headers,
                    Proxy proxy,
                    byte[] body,
                    boolean followRedirects) {
        this.method = method;
        this.url = url;
        this.headers = headers;
        this.proxy = proxy;
        this.body = body;
        this.followRedirects = followRedirects;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public URL getUrl() {
        return url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public byte[] getBody() {
        return body;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public Response send() throws IOException {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection(proxy);
            conn.setRequestMethod(method.name());
            conn.setInstanceFollowRedirects(followRedirects);
            headers.forEach(conn::setRequestProperty);

            if (method.equals(HttpMethod.PUT) || method.equals(HttpMethod.POST)) {
                conn.setRequestProperty("Content-Length", Integer.toString(body.length));

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body);
                    os.flush();
                }
            }

            try (InputStream is = conn.getInputStream()) {
                return new Response(this, HttpStatus.from(conn.getResponseCode()),
                        IOHelper.toByteArray(is), conn.getHeaderFields());
            }
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // TODO: Create a proper executor service to submit these to.
    public void sendNonBlocking(Consumer<Response> onSuccess, Consumer<Exception> onError) {
        new Thread(() -> {
            try {
                Response response = send();
                onSuccess.accept(response);
            } catch (Exception exception) {
                onError.accept(exception);
            }
        }, "http4j_client_request").start();
    }

    public static class Builder {
        private HttpMethod method = HttpMethod.GET;
        private URL url;
        private Map<String, String> headers = new HashMap<>();
        private Proxy proxy = Proxy.NO_PROXY;
        private byte[] body = new byte[0];
        private boolean followRedirects;

        Builder() {
        }

        public Builder setMethod(HttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder setUrl(URL url) {
            this.url = url;
            return this;
        }

        public Builder setUrl(URI uri) throws MalformedURLException {
            return setUrl(uri.toURL());
        }

        public Builder setUrl(String url) throws MalformedURLException {
            return setUrl(URI.create(url));
        }

        public Builder setHeaders(Map<String, String> headers) {
            if (headers == null) headers = new HashMap<>();

            this.headers = headers;
            return this;
        }

        public Builder setHeader(String key, String value) {
            headers.put(key, value);
            return this;
        }

        public Builder addHeaders(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        public Builder setProxy(Proxy proxy) {
            if (proxy == null) proxy = Proxy.NO_PROXY;

            this.proxy = proxy;
            return this;
        }

        public Builder setBody(byte[] body) {
            if (body == null) body = new byte[0];

            this.body = body;
            return this;
        }

        public Builder setBody(String body, Charset charset) {
            return setBody(body.getBytes(charset));
        }

        public Builder setBody(String body) {
            return setBody(body, StandardCharsets.UTF_8);
        }

        public Builder setFollowRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }

        public Request create() {
            return new Request(method, url, headers, proxy, body, followRedirects);
        }
    }
}