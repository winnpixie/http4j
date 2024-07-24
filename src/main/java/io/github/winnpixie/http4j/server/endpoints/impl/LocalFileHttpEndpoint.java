package io.github.winnpixie.http4j.server.endpoints.impl;

import io.github.winnpixie.http4j.server.incoming.HttpRequest;
import io.github.winnpixie.http4j.server.endpoints.HttpEndpoint;
import io.github.winnpixie.http4j.shared.HttpResponseStatus;
import io.github.winnpixie.http4j.shared.utilities.FileHelper;
import io.github.winnpixie.http4j.shared.utilities.IOHelper;

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

            try (ByteArrayOutputStream bodyStream = response.getBody()) {
                FileInputStream fileStream = new FileInputStream(file);
                IOHelper.transfer(fileStream, bodyStream);

                response.setHeader("Content-Length", Integer.toString(bodyStream.size()));

                // Wow, this looks so stupid.
                fileStream.close();
                fileStream = new FileInputStream(file);
                String mime = FileHelper.guessMime(fileStream);
                fileStream.close();

                if (mime.equals("application/octet-stream")) mime = FileHelper.guessMime(file.getName());
                response.setHeader("Content-Type", mime);

                response.setStatus(HttpResponseStatus.OK);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
