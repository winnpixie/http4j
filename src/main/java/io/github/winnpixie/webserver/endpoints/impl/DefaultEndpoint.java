package io.github.winnpixie.webserver.endpoints.impl;

import io.github.winnpixie.webserver.endpoints.Endpoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class DefaultEndpoint extends Endpoint {
    public DefaultEndpoint() {
        super("/", response -> {
            var request = response.getRequest();
            var file = new File(request.getRequestThread().getServer().getRootDirectory(), request.getPath());
            if (file.isDirectory()) file = new File(file, "index.html");

            if (!file.isDirectory() && request.getPath().endsWith("/")) {
                response.setCode(404);
                response.setCodeInfo("Not Found");
                return;
            }

            try (var body = response.getBody()) {
                if (!file.getCanonicalPath().startsWith(request.getRequestThread().getServer().getRootDirectory().getCanonicalPath())
                        || !file.exists()) {
                    if (file.exists()) {
                        System.out.println("Prevented read from file outside of server root directory");
                    }

                    response.setCode(404);
                    response.setCodeInfo("Not Found");
                } else {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        int ch;
                        while ((ch = reader.read()) != -1) {
                            body.write(ch);
                        }
                    }

                    response.setCode(200);
                    response.setCodeInfo("OK");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
