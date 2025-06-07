package io.github.foss4j.http4j.client;

import io.github.foss4j.http4j.shared.HttpStatus;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public record Response(Request request,
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