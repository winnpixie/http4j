package io.github.winnpixie.http4j.shared;

public enum HttpMethod {
    HEAD,
    GET,
    DELETE,
    PUT,
    POST,
    // N/A
    UNKNOWN;

    public static HttpMethod from(String verb) {
        for (HttpMethod method : values()) {
            if (method.name().equalsIgnoreCase(verb)) return method;
        }

        return UNKNOWN;
    }
}
