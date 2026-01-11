package util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JsonUtils {
    public static Map<String, String> parseJson(String body) {
        if (body == null || body.isBlank()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        String trimmed = body.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        String[] pairs = trimmed.split(",");
        for (String pair : pairs) {
            String[] parts = pair.split(":", 2);
            if (parts.length != 2) {
                continue;
            }
            String key = unquote(parts[0].trim());
            String value = unquote(parts[1].trim());
            result.put(key, value);
        }
        return result;
    }

    public static String error(String message) {
        return String.format("{\"success\":false,\"description\":\"%s\"}", escapeJson(message));
    }

    public static String successWithMessage(String message) {
        return String.format("{\"success\":true,\"description\":\"%s\"}", escapeJson(message));
    }

    public static String mapToJson(Map<String, Integer> map) {
        StringBuilder json = new StringBuilder("{");
        int index = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (index > 0) {
                json.append(",");
            }
            json.append("\"").append(escapeJson(entry.getKey())).append("\":").append(entry.getValue());
            index++;
        }
        json.append("}");
        return json.toString();
    }

    private static String unquote(String value) {
        String trimmed = value.trim();
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
                || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    public static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
