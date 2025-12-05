package io.github.winnpixie.http4j.shared.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileHelper {
    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private static final Properties KNOWN_CONTENT_TYPES = new Properties();

    static {
        try {
            KNOWN_CONTENT_TYPES.load(FileHelper.class.getResourceAsStream("/content-types.properties"));
        } catch (IOException ioe) {
            Logger.getGlobal().log(Level.WARNING, "Error retrieving built-in Content-Type map", ioe);
        }
    }

    private FileHelper() {
    }

    public static String getContentType(String fileName) {
        String internalGuess = URLConnection.guessContentTypeFromName(fileName);
        if (internalGuess != null) {
            return internalGuess;
        }

        int extIdx = fileName.lastIndexOf('.');
        if (extIdx < 0 || extIdx + 1 == fileName.length()) {
            return DEFAULT_CONTENT_TYPE;
        }

        return KNOWN_CONTENT_TYPES.getProperty(fileName.substring(extIdx + 1).toLowerCase(), DEFAULT_CONTENT_TYPE);
    }

    public static String getContentType(InputStream input) {
        try {
            String internalGuess = URLConnection.guessContentTypeFromStream(input);
            if (internalGuess != null) {
                return internalGuess;
            }
        } catch (IOException ignored) {
            //  return the default type on exception.
        }

        return DEFAULT_CONTENT_TYPE;
    }
}
