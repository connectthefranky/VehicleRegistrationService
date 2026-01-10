import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    private static final int DEFAULT_PORT = 8080;
    private static final Map<String, String> ACCOUNTS = new ConcurrentHashMap<>();
    private static final Map<String, RegistrationEntry> REGISTRATIONS = new ConcurrentHashMap<>();
    private static final Map<String, Integer> STATISTICS = new ConcurrentHashMap<>();
    private static final Random RANDOM = new Random();

    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid port provided, defaulting to " + DEFAULT_PORT);
                port = DEFAULT_PORT;
            }
        }
        HttpServer server = startServer(port);
        System.out.println("Vehicle Registration Service started on port " + server.getAddress().getPort());
    }

    static HttpServer startServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/account", new AccountHandler());
        server.createContext("/register", new RegisterHandler());
        server.createContext("/statistics", new StatisticsHandler());
        server.createContext("/registration", new RegistrationCheckHandler());
        server.createContext("/help", new HelpHandler());
        server.createContext("/gui", new GuiHandler());
        server.setExecutor(null);
        server.start();
        return server;
    }

    private static class AccountHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"success\":false,\"description\":\"Method not allowed\"}");
                return;
            }
            String body = readBody(exchange.getRequestBody());
            Map<String, String> payload = parseJson(body);
            String accountId = payload.get("accountId");
            if (accountId == null || accountId.isBlank()) {
                sendJson(exchange, 400, "{\"success\":false,\"description\":\"accountId is required\"}");
                return;
            }
            if (ACCOUNTS.containsKey(accountId)) {
                sendJson(exchange, 409, "{\"success\":false,\"description\":\"Provided account ID already exists.\"}");
                return;
            }
            String password = generatePassword(8);
            ACCOUNTS.put(accountId, password);
            STATISTICS.put(accountId, 0);
            String response = String.format("{\"success\":true,\"description\":\"Your account has been created.\",\"password\":\"%s\"}", password);
            sendJson(exchange, 201, response);
        }
    }

    private static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"success\":false,\"description\":\"Method not allowed\"}");
                return;
            }
            AuthResult auth = authenticate(exchange.getRequestHeaders());
            if (!auth.success) {
                sendJson(exchange, 401, "{\"success\":false,\"description\":\"Unauthorized\"}");
                return;
            }
            String body = readBody(exchange.getRequestBody());
            Map<String, String> payload = parseJson(body);
            String registrationCode = payload.get("registrationCode");
            String validUntil = payload.get("validUntil");
            if (registrationCode == null || registrationCode.isBlank() || validUntil == null || validUntil.isBlank()) {
                sendJson(exchange, 400, "{\"success\":false,\"description\":\"registrationCode and validUntil are required\"}");
                return;
            }
            if (REGISTRATIONS.containsKey(registrationCode)) {
                sendJson(exchange, 409, "{\"success\":false,\"description\":\"Provided registration code already exists.\"}");
                return;
            }
            LocalDate date;
            try {
                date = LocalDate.parse(validUntil);
            } catch (DateTimeParseException ex) {
                sendJson(exchange, 400, "{\"success\":false,\"description\":\"validUntil must be ISO date (YYYY-MM-DD)\"}");
                return;
            }
            REGISTRATIONS.put(registrationCode, new RegistrationEntry(auth.accountId, date));
            STATISTICS.computeIfPresent(auth.accountId, (key, value) -> value + 1);
            sendJson(exchange, 201, "{\"success\":true,\"description\":\"Vehicle registered successfully.\"}");
        }
    }

    private static class StatisticsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"success\":false,\"description\":\"Method not allowed\"}");
                return;
            }
            AuthResult auth = authenticate(exchange.getRequestHeaders());
            if (!auth.success) {
                sendJson(exchange, 401, "{\"success\":false,\"description\":\"Unauthorized\"}");
                return;
            }
            StringBuilder json = new StringBuilder("{");
            int index = 0;
            for (Map.Entry<String, Integer> entry : STATISTICS.entrySet()) {
                if (index > 0) {
                    json.append(",");
                }
                json.append("\"").append(escapeJson(entry.getKey())).append("\":").append(entry.getValue());
                index++;
            }
            json.append("}");
            sendJson(exchange, 200, json.toString());
        }
    }

    private static class RegistrationCheckHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"success\":false,\"description\":\"Method not allowed\"}");
                return;
            }
            URI uri = exchange.getRequestURI();
            String[] parts = uri.getPath().split("/");
            if (parts.length < 3 || parts[2].isBlank()) {
                sendJson(exchange, 400, "{\"success\":false,\"description\":\"registrationCode is required in path\"}");
                return;
            }
            String registrationCode = parts[2];
            RegistrationEntry entry = REGISTRATIONS.get(registrationCode);
            if (entry == null) {
                sendJson(exchange, 404, "{\"success\":false,\"description\":\"Registration not found\"}");
                return;
            }
            LocalDate today = LocalDate.now();
            String message = entry.validUntil.isBefore(today)
                    ? "Your registration has expired."
                    : "Your registration is still valid.";
            String response = String.format("{\"validUntil\":\"%s\",\"message\":\"%s\"}", entry.validUntil, message);
            sendJson(exchange, 200, response);
        }
    }

    private static class HelpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendText(exchange, 200, helpPage());
                return;
            }
            sendText(exchange, 200, helpPage());
        }
    }

    private static class GuiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendText(exchange, 405, "Method not allowed");
                return;
            }
            sendHtml(exchange, 200, guiPage());
        }
    }

    private static AuthResult authenticate(Headers headers) {
        String authHeader = headers.getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return AuthResult.failure();
        }
        String encoded = authHeader.substring("Basic ".length());
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return AuthResult.failure();
        }
        int index = decoded.indexOf(':');
        if (index < 0) {
            return AuthResult.failure();
        }
        String accountId = decoded.substring(0, index);
        String password = decoded.substring(index + 1);
        String stored = ACCOUNTS.get(accountId);
        if (stored == null || !stored.equals(password)) {
            return AuthResult.failure();
        }
        return AuthResult.success(accountId);
    }

    private static String readBody(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendText(HttpExchange exchange, int status, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendHtml(HttpExchange exchange, int status, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseJson(String body) {
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

    private static String unquote(String value) {
        String trimmed = value.trim();
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
                || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String generatePassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return builder.toString();
    }

    private static String helpPage() {
        return "Vehicle Registration Service Help\n"
                + "\n"
                + "Configuration & Run\n"
                + "1) Ensure Java 17+ is installed.\n"
                + "2) Compile: javac src/Main.java\n"
                + "3) Run: java -cp src Main\n"
                + "4) Service will be available at http://localhost:8080\n"
                + "\n"
                + "Endpoints\n"
                + "POST /account\n"
                + "  Body: {\"accountId\":\"user@example.com\"}\n"
                + "POST /register\n"
                + "  Header: Authorization: Basic base64(accountId:password)\n"
                + "  Body: {\"registrationCode\":\"555K487\",\"validUntil\":\"2024-11-18\"}\n"
                + "GET /statistics\n"
                + "  Header: Authorization: Basic base64(accountId:password)\n"
                + "GET /registration/{registrationCode}\n"
                + "GET /gui (simple web UI)\n";
    }

    private static String guiPage() {
        return "<!DOCTYPE html>\n"
                + "<html lang=\"en\">\n"
                + "<head><meta charset=\"utf-8\"/><title>Vehicle Registration Service</title></head>\n"
                + "<body>\n"
                + "<h1>Vehicle Registration Service</h1>\n"
                + "<section>\n"
                + "<h2>Create Account</h2>\n"
                + "<input id=\"accountId\" placeholder=\"accountId\"/>\n"
                + "<button onclick=\"createAccount()\">Create</button>\n"
                + "<pre id=\"accountResult\"></pre>\n"
                + "</section>\n"
                + "<section>\n"
                + "<h2>Register Vehicle</h2>\n"
                + "<input id=\"regAccountId\" placeholder=\"accountId\"/>\n"
                + "<input id=\"regPassword\" placeholder=\"password\"/>\n"
                + "<input id=\"registrationCode\" placeholder=\"registrationCode\"/>\n"
                + "<input id=\"validUntil\" placeholder=\"YYYY-MM-DD\"/>\n"
                + "<button onclick=\"registerVehicle()\">Register</button>\n"
                + "<pre id=\"registerResult\"></pre>\n"
                + "</section>\n"
                + "<section>\n"
                + "<h2>Check Registration</h2>\n"
                + "<input id=\"checkCode\" placeholder=\"registrationCode\"/>\n"
                + "<button onclick=\"checkRegistration()\">Check</button>\n"
                + "<pre id=\"checkResult\"></pre>\n"
                + "</section>\n"
                + "<section>\n"
                + "<h2>Statistics</h2>\n"
                + "<input id=\"statsAccountId\" placeholder=\"accountId\"/>\n"
                + "<input id=\"statsPassword\" placeholder=\"password\"/>\n"
                + "<button onclick=\"loadStatistics()\">Load</button>\n"
                + "<pre id=\"statsResult\"></pre>\n"
                + "</section>\n"
                + "<script>\n"
                + "async function createAccount() {\n"
                + "  const accountId = document.getElementById('accountId').value;\n"
                + "  const response = await fetch('/account', {\n"
                + "    method: 'POST',\n"
                + "    headers: {'Content-Type': 'application/json'},\n"
                + "    body: JSON.stringify({accountId})\n"
                + "  });\n"
                + "  document.getElementById('accountResult').textContent = await response.text();\n"
                + "}\n"
                + "async function registerVehicle() {\n"
                + "  const accountId = document.getElementById('regAccountId').value;\n"
                + "  const password = document.getElementById('regPassword').value;\n"
                + "  const registrationCode = document.getElementById('registrationCode').value;\n"
                + "  const validUntil = document.getElementById('validUntil').value;\n"
                + "  const response = await fetch('/register', {\n"
                + "    method: 'POST',\n"
                + "    headers: {\n"
                + "      'Content-Type': 'application/json',\n"
                + "      'Authorization': 'Basic ' + btoa(accountId + ':' + password)\n"
                + "    },\n"
                + "    body: JSON.stringify({registrationCode, validUntil})\n"
                + "  });\n"
                + "  document.getElementById('registerResult').textContent = await response.text();\n"
                + "}\n"
                + "async function checkRegistration() {\n"
                + "  const registrationCode = document.getElementById('checkCode').value;\n"
                + "  const response = await fetch('/registration/' + registrationCode);\n"
                + "  document.getElementById('checkResult').textContent = await response.text();\n"
                + "}\n"
                + "async function loadStatistics() {\n"
                + "  const accountId = document.getElementById('statsAccountId').value;\n"
                + "  const password = document.getElementById('statsPassword').value;\n"
                + "  const response = await fetch('/statistics', {\n"
                + "    headers: {\n"
                + "      'Authorization': 'Basic ' + btoa(accountId + ':' + password)\n"
                + "    }\n"
                + "  });\n"
                + "  document.getElementById('statsResult').textContent = await response.text();\n"
                + "}\n"
                + "</script>\n"
                + "</body></html>\n";
    }

    private static class RegistrationEntry {
        private final String accountId;
        private final LocalDate validUntil;

        private RegistrationEntry(String accountId, LocalDate validUntil) {
            this.accountId = accountId;
            this.validUntil = validUntil;
        }
    }

    private static class AuthResult {
        private final boolean success;
        private final String accountId;

        private AuthResult(boolean success, String accountId) {
            this.success = success;
            this.accountId = accountId;
        }

        private static AuthResult success(String accountId) {
            return new AuthResult(true, accountId);
        }

        private static AuthResult failure() {
            return new AuthResult(false, null);
        }
    }
}
