package io.github.winnpixie.http4j.shared.utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOHelper {
    private IOHelper() {
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        return transfer(input, new ByteArrayOutputStream()).toByteArray();
    }

    public static <T extends OutputStream> T transfer(InputStream from, T to) throws IOException {
        byte[] buffer = new byte[1024]; // 1K buffer

        int read;
        while ((read = from.read(buffer)) != -1) {
            to.write(buffer, 0, read);
        }

        return to;
    }
}