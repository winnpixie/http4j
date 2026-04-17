package io.github.winnpixie.http4j.client;

import io.github.winnpixie.http4j.shared.HttpMethod;
import io.github.winnpixie.http4j.shared.throwables.HttpException;

import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private final HttpMethod method;
    private final URL url;
    private final Map<String, String> headers;
    private final Proxy proxy;
    private final byte[] body;
    private final boolean followRedirects;

    HttpRequest(Builder builder) {
        this.method = builder.method;
        this.url = builder.url;
        this.headers = builder.headers;
        this.proxy = builder.proxy;
        this.body = builder.body;
        this.followRedirects = builder.followRedirects;
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

        public Builder setUrl(URI uri) throws HttpException {
            try {
                return setUrl(uri.toURL());
            } catch (MalformedURLException mue) {
                throw new HttpException("Invalid URI", mue);
            }
        }

        public Builder setUrl(String url) throws HttpException {
            return setUrl(URI.create(url));
        }

        public Builder setHeaders(Map<String, String> headers) {
            if (headers == null) {
                headers = new HashMap<>();
            }

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
            if (proxy == null) {
                proxy = Proxy.NO_PROXY;
            }

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

        public HttpRequest build() {
            return new HttpRequest(this);
        }
    }
}