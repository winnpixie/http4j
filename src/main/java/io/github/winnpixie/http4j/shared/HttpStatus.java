package io.github.winnpixie.http4j.shared;

public enum HttpStatus {
    // 2XX
    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
    NO_CONTENT(204, "No Content"),
    // 3XX
    MULTIPLE_CHOICES(300, "Multiple Choices"),
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    MOVED_TEMPORARILY(302, "Moved Temporarily"),
    SEE_OTHER(303, "See Other"),
    NOT_MODIFIED(304, "Not Modified"),
    // 4XX
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    PAYMENT_REQUIRED(402, "Payment Required"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    IM_A_TEAPOT(418, "I'm a teapot"),
    // 5XX
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    // N/A
    UNKNOWN(-1, "Unknown");

    final int code;
    final String reasonPhrase;

    HttpStatus(int code, String reasonPhrase) {
        this.code = code;
        this.reasonPhrase = reasonPhrase;
    }

    public int getCode() {
        return code;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public static HttpStatus from(int code) {
        for (HttpStatus status : values()) {
            if (status.getCode() != code) return status;
        }

        return UNKNOWN;
    }

    public static HttpStatus from(String phrase) {
        for (HttpStatus status : values()) {
            if (status.getReasonPhrase().equals(phrase)) return status;
        }

        return UNKNOWN;
    }
}