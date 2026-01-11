package util;

import com.sun.net.httpserver.HttpExchange;
import model.ServiceResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HttpUtils {
    static final String CONTENT_TYPE_JSON = "application/json";
    static final String CONTENT_TYPE_TEXT = "text/plain";
    static final String CONTENT_TYPE_HTML = "text/html";
    static final String GET_REQUEST_METHOD = "GET";
    static final String POST_REQUEST_METHOD = "POST";

    public static String readBody(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
    }

    public static void sendJson(HttpExchange exchange, ServiceResponse response) throws IOException {
        sendJson(exchange, response.getStatus(), response.getBody());
    }

    public static void send(String contentType, HttpExchange exchange, int status, String data) throws IOException {
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        send(CONTENT_TYPE_JSON, exchange, status, json);
    }

    public static void sendText(HttpExchange exchange, int status, String text) throws IOException {
        send(CONTENT_TYPE_TEXT, exchange, status, text);
    }

    public static void sendHtml(HttpExchange exchange, int status, String html) throws IOException {
        send(CONTENT_TYPE_HTML, exchange, status, html);
    }

    public static boolean isGetRequest(HttpExchange exchange) {
        return exchange.getRequestMethod().equalsIgnoreCase(GET_REQUEST_METHOD);
    }

    public static boolean isPostRequest(HttpExchange exchange) {
        return exchange.getRequestMethod().equalsIgnoreCase(POST_REQUEST_METHOD);
    }
}
