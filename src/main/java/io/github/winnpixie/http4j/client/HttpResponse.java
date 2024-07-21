package io.github.winnpixie.http4j.client;

import io.github.winnpixie.http4j.shared.HttpResponseStatus;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class HttpResponse {
    private final HttpRequest request;
    private final HttpResponseStatus status;
    private final byte[] body;
    private final Map<String, List<String>> headers;

    public HttpResponse(HttpRequest request, HttpResponseStatus status, byte[] body, Map<String, List<String>> headers) {
        this.request = request;
        this.status = status;
        this.body = body;
        this.headers = headers;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public HttpResponseStatus getStatus() {
        return status;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    public String getBodyAsString(Charset charset) {
        return new String(body, charset);
    }

    public String getBodyAsString() {
        return getBodyAsString(StandardCharsets.UTF_8);
    }
}