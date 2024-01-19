package io.github.winnpixie.httpsrv.endpoints.impl;

import io.github.winnpixie.httpsrv.direction.incoming.Request;
import io.github.winnpixie.httpsrv.direction.outgoing.ResponseStatus;
import io.github.winnpixie.httpsrv.endpoints.Endpoint;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DefaultEndpoint extends Endpoint {
    public DefaultEndpoint() {
        super("/", response -> {
            Request request = response.getRequest();
            String path = request.getPath().substring(1);
            File file = new File(request.getRequestThread().getServer().getRoot(), path);
            if (file.isDirectory()) file = new File(file, "index.html");

            if (!file.exists()) {
                response.setStatus(ResponseStatus.NOT_FOUND);
                return;
            }

            if (!file.isDirectory() && path.endsWith("/")) {
                response.setStatus(ResponseStatus.NOT_FOUND);
                return;
            }

            try {
                String canonicalPath = file.getCanonicalPath();
                String canonicalRoot = request.getRequestThread().getServer().getRoot().getCanonicalPath();
                if (!canonicalPath.startsWith(canonicalRoot)) {
                    request.getRequestThread().getServer().getLogger()
                            .warning("Prevented read from file outside of server root!");

                    response.setStatus(ResponseStatus.NOT_FOUND);
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

                response.setStatus(ResponseStatus.OK);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
