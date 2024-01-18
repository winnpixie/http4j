package io.github.winnpixie.httpsrv.direction.incoming;

public enum RequestMethod {
    UNKNOWN,
    GET, POST, HEAD;

    public static RequestMethod fromName(String name) {
        for (RequestMethod method : values()) {
            if (!method.name().equalsIgnoreCase(name)) continue;

            return method;
        }

        return UNKNOWN;
    }
}
