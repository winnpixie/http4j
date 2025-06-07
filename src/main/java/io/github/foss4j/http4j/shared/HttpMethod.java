package io.github.foss4j.http4j.shared;

public enum HttpMethod {
    UNKNOWN,
    HEAD,
    GET,
    DELETE,
    PUT,
    POST;

    public static HttpMethod from(String verb) {
        for (HttpMethod method : values()) {
            if (method.name().equalsIgnoreCase(verb)) return method;
        }

        return UNKNOWN;
    }
}
