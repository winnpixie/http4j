package io.github.winnpixie.http4j.client;

import io.github.winnpixie.http4j.shared.HttpStatus;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public record HttpResponse(HttpRequest request,
                           HttpStatus status,
                           byte[] body,
                           Map<String, List<String>> headers) {
    public String bodyAsString(Charset charset) {
        return new String(body, charset);
    }

    public String bodyAsString() {
        return bodyAsString(StandardCharsets.UTF_8);
    }
}