package io.github.winnpixie.http4j.server;

import io.github.winnpixie.http4j.server.handlers.PathHandler;
import io.github.winnpixie.http4j.server.incoming.HttpRequest;
import io.github.winnpixie.http4j.server.incoming.HttpRequestProvider;
import io.github.winnpixie.http4j.server.outgoing.HttpResponse;
import io.github.winnpixie.http4j.server.outgoing.HttpResponseWriter;
import io.github.winnpixie.http4j.shared.HttpMethod;
import io.github.winnpixie.http4j.shared.HttpStatus;
import io.github.winnpixie.http4j.shared.throwables.HttpException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.logging.Level;

public class HttpServerThread extends Thread {
    private final HttpServer server;

    public HttpServerThread(HttpServer server) {
        super("http4j_server");

        this.server = server;
    }

    @Override
    public void run() {
        try (Selector selector = Selector.open();
             ServerSocketChannel srvChannel = ServerSocketChannel.open()) {
            srvChannel.configureBlocking(false);
            srvChannel.bind(new InetSocketAddress(server.getPort()), server.getConnectionLimit());
            srvChannel.register(selector, SelectionKey.OP_ACCEPT);

            InetSocketAddress address = (InetSocketAddress) srvChannel.getLocalAddress();
            server.getLogger().info(() -> String.format("http4j (server) started at %s:%d",
                    address.getHostName(), address.getPort()));

            while (server.isRunning()) {
                int queue = selector.selectNow();
                if (queue < 1) {
                    continue;
                }

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();

                    if (key.isAcceptable()) { // Server Poll
                        accept(srvChannel, selector);
                    } else if (key.isReadable() && key.isWritable()) { // Client Process
                        processClient(key);
                    }

                    keys.remove();
                }
            }
        } catch (IOException ioe) {
            server.getLogger().log(Level.WARNING, "Error opening channels", ioe);
        }
    }

    private void accept(ServerSocketChannel serverChannel, Selector selector) throws IOException {
        SocketChannel channel = serverChannel.accept();

        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    private void processClient(SelectionKey key) {
        try (SocketChannel channel = (SocketChannel) key.channel()) {
            HttpRequest request = read(channel);
            HttpResponse response = write(request);

            server.getLogger().info(() -> {
                try {
                    return String.format("addr=%s/x-fwd=%s path='%s' status-code=%d user-agent='%s'",
                            ((InetSocketAddress) channel.getRemoteAddress()).getAddress().getHostAddress(),
                            request.getHeader("X-Forwarded-For", true),
                            request.getPath(),
                            response.getStatus().getCode(),
                            request.getHeader("User-Agent", true));
                } catch (IOException ioe) {
                    server.getLogger().log(Level.WARNING, "Error retrieving client address", ioe);

                    return String.format("addr=err/x-fwd=%s path='%s' status-code=%d user-agent='%s'",
                            request.getHeader("X-Forwarded-For", true),
                            request.getPath(),
                            response.getStatus().getCode(),
                            request.getHeader("User-Agent", true));
                }
            });
        } catch (IOException | HttpException exc) {
            server.getLogger().log(Level.WARNING, "Error processing request", exc);
        }
    }

    private HttpRequest read(SocketChannel channel) throws HttpException {
        return HttpRequestProvider.create(server, channel);
    }

    private HttpResponse write(HttpRequest request) throws HttpException {
        HttpResponse response = null;

        if (request.getHeader("Host", true).isEmpty()) {
            response = new HttpResponse.Builder()
                    .setStatus(HttpStatus.BAD_REQUEST)
                    .build();
        } else {
            PathHandler handler = server.getPathHandlers().getHandler(request.getPath());
            if (handler != null) {
                response = handler.process(request);
            }
        }

        if (response == null) {
            response = new HttpResponse.Builder().build();
        }

        HttpResponseWriter.writeHead(response, request.getChannel());
        if (request.getMethod() != HttpMethod.HEAD) {
            HttpResponseWriter.writeBody(response, request.getChannel());
        }

        return response;
    }
}
