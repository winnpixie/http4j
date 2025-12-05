package io.github.winnpixie.http4j.client;

import io.github.winnpixie.http4j.shared.utilities.Constants;

public class HttpClient {
    HttpClient() {
    }

    public static HttpClient newClient() {
        return new HttpClient();
    }

    public Request.Builder newRequest() {
        return new Request.Builder()
                .setHeader("User-Agent", Constants.CLIENT_ID);
    }
}