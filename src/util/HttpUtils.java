package util;

import com.sun.net.httpserver.HttpExchange;
import model.ServiceResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HttpUtils {
    public final static String CONTENT_TYPE_HTML = "text/html";
    public final static String CONTENT_TYPE_JSON = "application/json";
    public final static String CONTENT_TYPE_TEXT = "text/plain";
    public static final String REQUEST_METHOD_GET = "GET";
    public static final String REQUEST_METHOD_POST = "POST";
    public static final String METHOD_NOT_ALLOWED = "Method not allowed";

    public static String readBody(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
    }

    public static void sendJson(HttpExchange exchange, ServiceResponse response) {
        sendJson(exchange, response.getStatus(), response.getBody());
    }

    public static void sendJson(HttpExchange exchange, int status, String json) {
        sendResponse(CONTENT_TYPE_JSON, exchange, status, json);
    }

    public static void sendJsonMethodNotAllowed(HttpExchange exchange) {
        sendJson(exchange, 405, METHOD_NOT_ALLOWED);
    }

    public static void sendJsonUnauthorized(HttpExchange exchange) {
        sendJson(exchange, 401, JsonUtils.error("Unauthorized"));
    }

    public static void sendText(HttpExchange exchange, int status, String text) {
        sendResponse(CONTENT_TYPE_TEXT, exchange, status, text);
    }

    public static void sendTextMethodNotAllowed(HttpExchange exchange) {
        sendText(exchange, 405, METHOD_NOT_ALLOWED);
    }

    public static void sendHtml(HttpExchange exchange, int status, String html) {
        sendResponse(CONTENT_TYPE_HTML, exchange, status, html);
    }

    public static void sendResponse(String contentType, HttpExchange exchange, int status, String html) {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        try {
            exchange.sendResponseHeaders(status, bytes.length);
        } catch (IOException exception) {
            System.out.println("Exception while sending response headers: " + exception.getMessage());
        }
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        } catch (IOException exception) {
            System.out.println("Exception while writing to output stream: " + exception.getMessage());
        }
    }
    public static boolean isGetRequest(HttpExchange exchange) {
        return exchange.getRequestMethod().equalsIgnoreCase(REQUEST_METHOD_GET);
    }

    public static boolean isPostRequest(HttpExchange exchange) {
        return exchange.getRequestMethod().equalsIgnoreCase(REQUEST_METHOD_POST);
    }
}

