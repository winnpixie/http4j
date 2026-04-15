package io.github.winnpixie.http4j.shared.utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class IOHelper {
    private IOHelper() {
    }

    /* IO */
    public static byte[] toByteArray(InputStream src) throws IOException {
        return transfer(src, new ByteArrayOutputStream()).toByteArray();
    }

    public static <T extends OutputStream> T transfer(InputStream src, T dst) throws IOException {
        byte[] buffer = new byte[1024]; // 1K buffer

        int len;
        while ((len = src.read(buffer)) != -1) {
            dst.write(buffer, 0, len);
        }

        return dst;
    }

    /* NIO */
    public static byte[] toByteArray(ByteBuffer src) {
        int size = src.remaining();
        byte[] dst = new byte[size];

        for (int i = 0; i < size; i++) {
            dst[i] = src.get();
        }

        return dst;
    }

    public static String readLine(ByteBuffer src) {
        if (!src.hasRemaining()) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        while (src.hasRemaining()) {
            byte ch = src.get();
            boolean eol = ch == '\r' || ch == '\n';
            if (ch == '\r') {
                eol = !src.hasRemaining() || (ch = src.get()) == '\n';
            }

            if (eol) {
                break;
            }

            builder.append((char) ch);
        }

        return builder.toString();
    }
}