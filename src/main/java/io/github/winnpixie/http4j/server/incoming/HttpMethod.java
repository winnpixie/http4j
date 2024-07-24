package io.github.winnpixie.http4j.server.incoming;

public enum HttpMethod {
    UNKNOWN,
    GET, POST, HEAD;

    public static HttpMethod fromName(String name) {
        for (HttpMethod method : values()) {
            if (!method.name().equalsIgnoreCase(name)) continue;

            return method;
        }

        return UNKNOWN;
    }
}
