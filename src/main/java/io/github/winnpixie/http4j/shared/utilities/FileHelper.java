package io.github.winnpixie.http4j.shared.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Properties;

public class FileHelper {
    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private static final Properties KNOWN_CONTENT_TYPES = new Properties();

    static {
        try {
            KNOWN_CONTENT_TYPES.load(FileHelper.class.getResourceAsStream("/content-types.properties"));
        } catch (IOException ignored) {
            // use default on unknown
        }
    }

    private FileHelper() {
    }

    public static String getContentType(String path) {
        String guess = URLConnection.guessContentTypeFromName(path);
        if (guess != null) {
            return guess;
        }

        int extIdx = path.lastIndexOf('.');
        if (extIdx < 0 || extIdx + 1 == path.length()) {
            return DEFAULT_CONTENT_TYPE;
        }

        String extension = path.substring(extIdx + 1).toLowerCase();
        return KNOWN_CONTENT_TYPES.getProperty(extension, DEFAULT_CONTENT_TYPE);
    }

    public static String getContentType(InputStream input) {
        try {
            String guess = URLConnection.guessContentTypeFromStream(input);
            if (guess != null) {
                return guess;
            }
        } catch (IOException ignored) {
            //  return default on throws
        }

        return DEFAULT_CONTENT_TYPE;
    }
}
