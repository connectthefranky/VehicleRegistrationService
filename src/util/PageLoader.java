package util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class PageLoader {
    private static final Path BASE_PATH = Path.of("src/static/view");

    public static String loadHtml(String filename) throws IOException {
        return load(filename, "<html><body>Missing page.</body></html>");
    }

    public static String loadText(String filename) throws IOException {
        return load(filename, "Help page not available.");
    }

    private static String load(String filename, String fallback) throws IOException {
        Path filePath = BASE_PATH.resolve(filename);
        if (!Files.exists(filePath)) {
            return fallback;
        }
        return Files.readString(filePath, StandardCharsets.UTF_8);
    }
}
