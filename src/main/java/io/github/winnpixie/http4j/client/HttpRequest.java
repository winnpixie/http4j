package io.github.winnpixie.http4j.client;

import io.github.winnpixie.http4j.shared.HttpMethod;

import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class HttpRequest {
    private HttpMethod method = HttpMethod.GET;
    private URL url;
    private Map<String, String> headers = new HashMap<>();
    private Proxy proxy = Proxy.NO_PROXY;
    private byte[] body;
    private boolean followRedirects;

    private Consumer<HttpResponse> onSuccess = response -> {
    };
    private Consumer<Throwable> onFailure = exception -> {
    };

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    public Consumer<HttpResponse> getOnSuccess() {
        return onSuccess;
    }

    public void setOnSuccess(Consumer<HttpResponse> onSuccess) {
        this.onSuccess = onSuccess;
    }

    public Consumer<Throwable> getOnFailure() {
        return onFailure;
    }

    public void setOnFailure(Consumer<Throwable> onFailure) {
        this.onFailure = onFailure;
    }
}