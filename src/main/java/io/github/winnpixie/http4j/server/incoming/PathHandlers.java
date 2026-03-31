package io.github.winnpixie.http4j.server.incoming;

import java.util.ArrayList;
import java.util.List;

public class PathHandlers {
    private final List<PathHandler> handlers = new ArrayList<>();

    public List<PathHandler> getHandlers() {
        return handlers;
    }

    public boolean add(PathHandler handler) {
        return handlers.add(handler);
    }

    public boolean remove(PathHandler handler) {
        return handlers.remove(handler);
    }

    public PathHandler getHandler(String path) {
        for (PathHandler handler : handlers) {
            if (path.startsWith(handler.getPath())) {
                return handler;
            }
        }

        return null;
    }
}
