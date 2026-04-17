package io.github.winnpixie.http4j.shared.throwables;

public class HttpException extends Exception {
    public HttpException(String message) {
        super(message);
    }

    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }
}
