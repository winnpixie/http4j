package io.github.winnpixie.webserver.endpoints.impl;

import io.github.winnpixie.webserver.endpoints.Endpoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DefaultEndpoint extends Endpoint {
    public DefaultEndpoint() {
        super("/", response -> {
            var request = response.getRequest();
            var file = new File(request.getRequestThread().getServer().getRootDirectory(), request.getPath());
            if (file.isDirectory()) file = new File(file, "index.html");

            if (!file.isDirectory() && request.getPath().endsWith("/")) {
                response.setStatusCode(404);
                response.setReasonPhrase("Not Found");
                return;
            }

            try (var body = response.getBody()) {
                if (!file.getCanonicalPath().startsWith(request.getRequestThread().getServer().getRootDirectory().getCanonicalPath())
                        || !file.exists()) {
                    if (file.exists()) {
                        response.getRequest().getRequestThread().getServer().getLogger()
                                .warning("Prevented read from file outside of server root directory");
                    }

                    response.setStatusCode(404);
                    response.setReasonPhrase("Not Found");
                } else {
                    try (var fileStream = new FileInputStream(file)) {
                        byte[] buffer = new byte[8192];
                        var read = -1;
                        while ((read = fileStream.read(buffer)) != -1) {
                            body.write(buffer, 0, read);
                        }
                    }

                    response.setStatusCode(200);
                    response.setReasonPhrase("OK");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
