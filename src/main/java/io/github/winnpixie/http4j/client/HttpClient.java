package io.github.winnpixie.http4j.client;

public class HttpClient {
    private HttpClient() {
    }

    public static HttpClient newClient() {
        return new HttpClient();
    }

    public Request.Builder newRequest() {
        Request.Builder builder = new Request.Builder();
        builder.setHeader("User-Agent", "http4j (client)");

        return builder;
    }
}