package io.github.winnpixie.http4j.shared;

public enum HttpMethod {
    GET("GET"),
    HEAD("HEAD"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    CONNECT("CONNECT"),
    OPTIONS("OPTIONS"),
    TRACE("TRACE"),
    // ?
    UNKNOWN("");

    final String verb;

    HttpMethod(String verb) {
        this.verb = verb;
    }

    public String getVerb() {
        return verb;
    }

    public static HttpMethod from(String verb) {
        for (HttpMethod method : values()) {
            if (method.getVerb().equalsIgnoreCase(verb)) {
                return method;
            }
        }

        return UNKNOWN;
    }
}
