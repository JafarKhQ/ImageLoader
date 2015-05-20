package com.epam.imageloader;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

final class StreamUtils {

    private static final int BUFFER_SIZE = 2048; //2K

    private StreamUtils() {
    }

    static long copyStream(InputStream from, OutputStream to) throws IOException {
        long total = 0;
        byte[] buffer = new byte[BUFFER_SIZE];

        while (true) {
            int r = from.read(buffer);
            if (r == -1) {
                break;
            }

            to.write(buffer, 0, r);
            total += r;
        }

        return total;
    }
}
