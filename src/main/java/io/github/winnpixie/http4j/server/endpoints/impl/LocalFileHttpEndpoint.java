package io.github.winnpixie.http4j.server.endpoints.impl;

import io.github.winnpixie.http4j.server.direction.incoming.HttpRequest;
import io.github.winnpixie.http4j.shared.HttpResponseStatus;
import io.github.winnpixie.http4j.server.endpoints.HttpEndpoint;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class LocalFileHttpEndpoint extends HttpEndpoint {
    public LocalFileHttpEndpoint() {
        super("/", response -> {
            HttpRequest request = response.getRequest();
            String path = request.getPath().substring(1);
            File file = new File(request.getRequestThread().getServer().getRoot(), path);
            if (!file.isDirectory() && path.endsWith("/")) {
                response.setStatus(HttpResponseStatus.NOT_FOUND);
                return;
            }

            if (file.isDirectory()) file = new File(file, "index.html");
            if (!file.exists()) {
                response.setStatus(HttpResponseStatus.NOT_FOUND);
                return;
            }

            try {
                String canonicalPath = file.getCanonicalPath();
                String canonicalRoot = request.getRequestThread().getServer().getRoot().getCanonicalPath();
                if (!canonicalPath.startsWith(canonicalRoot)) {
                    request.getRequestThread().getServer().getLogger()
                            .warning("Prevented read from file outside of server root!");

                    response.setStatus(HttpResponseStatus.NOT_FOUND);
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return; // TODO: Is this the correct approach?
            }

            try (ByteArrayOutputStream body = response.getBody();
                 FileInputStream fileStream = new FileInputStream(file)) {
                byte[] buffer = new byte[8192]; // 8K buffer
                int read;
                while ((read = fileStream.read(buffer)) != -1) {
                    body.write(buffer, 0, read);
                }

                response.setStatus(HttpResponseStatus.OK);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
