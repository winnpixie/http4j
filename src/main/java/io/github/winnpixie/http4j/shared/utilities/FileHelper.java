package io.github.winnpixie.http4j.shared.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Properties;

public class FileHelper {
    public static final String DEFAULT_MIME = "application/octet-stream";
    public static final Properties CUSTOM_MIMES;

    static {
        CUSTOM_MIMES = new Properties();

        try {
            CUSTOM_MIMES.load(FileHelper.class.getResourceAsStream("/mimes.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String guessMime(String fileName) {
        String type = URLConnection.guessContentTypeFromName(fileName);

        int extIdx = fileName.lastIndexOf('.');
        if (type == null && extIdx < 0 || extIdx + 1 == fileName.length()) return DEFAULT_MIME;
        if (type == null) return CUSTOM_MIMES.getProperty(fileName.substring(extIdx + 1).toLowerCase(), DEFAULT_MIME);

        return type;
    }

    public static String guessMime(InputStream input) {
        try {
            String type = URLConnection.guessContentTypeFromStream(input);
            if (type != null) return type;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return DEFAULT_MIME;
    }
}
