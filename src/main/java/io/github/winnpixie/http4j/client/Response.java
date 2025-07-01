package io.github.winnpixie.http4j.client;

import io.github.winnpixie.http4j.shared.HttpStatus;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class Response {
    private final Request request;
    private final HttpStatus status;
    private final byte[] body;
    private final Map<String, List<String>> headers;

    public Response(Request request, HttpStatus status, byte[] body, Map<String, List<String>> headers) {
        this.request = request;
        this.status = status;
        this.body = body;
        this.headers = headers;
    }

    public Request getRequest() {
        return request;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public byte[] getBody() {
        return body;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public String getBodyAsString(Charset charset) {
        return new String(body, charset);
    }

    public String getBodyAsString() {
        return getBodyAsString(StandardCharsets.UTF_8);
    }
}