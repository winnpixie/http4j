package io.github.winnpixie.http4j.shared.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Properties;

public class FileHelper {
    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    public static final Properties KNOWN_CONTENT_TYPES;

    static {
        KNOWN_CONTENT_TYPES = new Properties();

        try {
            KNOWN_CONTENT_TYPES.load(FileHelper.class.getResourceAsStream("/content-types.properties"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getContentType(String fileName) {
        String guess = URLConnection.guessContentTypeFromName(fileName);
        if (guess != null) return guess;

        int extIdx = fileName.lastIndexOf('.');
        if (extIdx < 0 || extIdx + 1 == fileName.length()) return DEFAULT_CONTENT_TYPE;

        return KNOWN_CONTENT_TYPES.getProperty(fileName.substring(extIdx + 1).toLowerCase(), DEFAULT_CONTENT_TYPE);
    }

    public static String getContentType(InputStream input) {
        try {
            String type = URLConnection.guessContentTypeFromStream(input);
            if (type != null) return type;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return DEFAULT_CONTENT_TYPE;
    }
}
