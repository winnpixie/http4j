package io.github.winnpixie.http4j.server.outgoing;

import io.github.winnpixie.http4j.shared.throwables.HttpException;
import io.github.winnpixie.http4j.shared.utilities.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpResponseWriter {
    private HttpResponseWriter() {
    }

    public static void writeHead(HttpResponse response, SocketChannel channel) throws HttpException {
        // Construct Head
        StringBuilder head = new StringBuilder("HTTP/1.1 ")
                .append(response.getStatus().getCode()).append(' ')
                .append(response.getStatus().getReasonPhrase())
                .append(Constants.END_OF_LINE);

        // Apply Headers
        for (Map.Entry<String, String> entry : response.getHeaders().entrySet()) {
            head.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append(Constants.END_OF_LINE);
        }
        head.append(Constants.END_OF_LINE);

        try {
            channel.write(StandardCharsets.UTF_8.encode(head.toString()));
        } catch (IOException ioe) {
            throw new HttpException("Error writing headers", ioe);
        }
    }

    public static void writeBody(HttpResponse response, SocketChannel channel) throws HttpException {
        byte[] content = response.getBody();

        // Generate simple error text if content body is empty
        if (content.length == 0 && response.getStatus().getCode() / 100 != 2) {
            content = String.format("%d - %s",
                            response.getStatus().getCode(),
                            response.getStatus().getReasonPhrase())
                    .getBytes(StandardCharsets.UTF_8);
        }

        try {
            channel.write(ByteBuffer.wrap(content));
        } catch (IOException ioe) {
            throw new HttpException("Error writing content body", ioe);
        }
    }
}
