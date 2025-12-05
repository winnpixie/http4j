package io.github.winnpixie.http4j.server.incoming;

import io.github.winnpixie.http4j.server.incoming.impl.FileRequestHandler;

import java.util.ArrayList;
import java.util.List;

public class RequestHandlers {
    private final FileRequestHandler defaultHandler = new FileRequestHandler();
    private final List<RequestHandler> handlers = new ArrayList<>();

    public FileRequestHandler getDefaultHandler() {
        return defaultHandler;
    }

    public List<RequestHandler> getHandlers() {
        return handlers;
    }

    public boolean add(RequestHandler handler) {
        return handlers.add(handler);
    }

    public boolean remove(RequestHandler handler) {
        return handlers.remove(handler);
    }

    public RequestHandler getHandler(String path) {
        for (RequestHandler handler : handlers) {
            if (path.startsWith(handler.getPath())) {
                return handler;
            }
        }

        return defaultHandler;
    }
}
