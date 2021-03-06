package com.dajudge.kindcontainer;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import static java.lang.System.currentTimeMillis;
import static java.nio.charset.StandardCharsets.UTF_8;

final class Utils {
    private Utils() {
    }

    @NotNull
    static String readString(final InputStream is) throws IOException {
        return new String(readBytes(is), UTF_8);
    }

    private static byte[] readBytes(final InputStream is) throws IOException {
        final byte[] buffer = new byte[1024];
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int read;
        while ((read = is.read(buffer)) > 0) {
            bos.write(buffer, 0, read);
        }
        return bos.toByteArray();
    }

    static String loadResource(final String name) {
        final InputStream stream = Utils.class.getClassLoader().getResourceAsStream(name);
        try (final InputStream is = stream) {
            return readString(is);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load resource: " + name, e);
        }
    }

    static <T> T waitUntilNotNull(
            final Supplier<T> check,
            final int timeout,
            final Supplier<String> errorMessage
    ) {
        final long start = currentTimeMillis();
        while ((currentTimeMillis() - start) < timeout) {
            final T result = check.get();
            if (result != null) {
                return result;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        throw new IllegalStateException(errorMessage.get());
    }

    public static String indent(final String prefix, final String string) {
        return string.replaceAll("(?m)^", prefix);
    }
}
