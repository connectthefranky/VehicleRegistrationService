import com.sun.net.httpserver.HttpServer;
import server.Server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ServiceTests {
    public static void main(String[] args) throws Exception {
        int port = findAvailablePort();
        HttpServer server = Server.startServer(port);
        try {
            testAccountCreation(port);
            String password = createAccount(port, "user@example.com");
            testDuplicateAccount(port);
            testRegisterVehicle(port, password);
            testDuplicateRegistration(port, password);
            testRegistrationCheck(port);
            testStatistics(port, password);
            System.out.println("All tests passed.");
        } finally {
            server.stop(0);
        }
    }

    private static void testAccountCreation(int port) throws Exception {
        String response = postJson(port, "/account", "{\"accountId\":\"test.account@example.com\"}", null);
        assertTrue(response.contains("\"success\":true"), "Account creation should succeed");
        assertTrue(response.contains("\"password\""), "Password should be generated");
    }

    private static void testDuplicateAccount(int port) throws Exception {
        int status = postJsonStatus(port, "/account", "{\"accountId\":\"test.account@example.com\"}", null);
        assertEquals(409, status, "Duplicate account should return 409");
    }

    private static void testRegisterVehicle(int port, String password) throws Exception {
        String auth = basicAuth("user@example.com", password);
        String body = "{\"registrationCode\":\"555K487\",\"validUntil\":\"2025-11-18\"}";
        String response = postJson(port, "/register", body, auth);
        assertTrue(response.contains("\"success\":true"), "Registration should succeed");
    }

    private static void testDuplicateRegistration(int port, String password) throws Exception {
        String auth = basicAuth("user@example.com", password);
        String body = "{\"registrationCode\":\"555K487\",\"validUntil\":\"2025-11-18\"}";
        int status = postJsonStatus(port, "/register", body, auth);
        assertEquals(409, status, "Duplicate registration should return 409");
    }

    private static void testRegistrationCheck(int port) throws Exception {
        String response = getJson(port, "/registration/555K487", null);
        assertTrue(response.contains("\"validUntil\":\"2025-11-18\""), "Valid until should match");
        assertTrue(response.contains("valid"), "Response should include validity message");
    }

    private static void testStatistics(int port, String password) throws Exception {
        String auth = basicAuth("user@example.com", password);
        String response = getJson(port, "/statistics", auth);
        assertTrue(response.contains("\"user@example.com\":1"), "Statistics should show 1 registration");
    }

    private static String createAccount(int port, String accountId) throws Exception {
        String response = postJson(port, "/account", "{\"accountId\":\"" + accountId + "\"}", null);
        String marker = "\"password\":\"";
        int start = response.indexOf(marker);
        if (start < 0) {
            throw new IllegalStateException("Password not found in response");
        }
        int valueStart = start + marker.length();
        int valueEnd = response.indexOf('"', valueStart);
        return response.substring(valueStart, valueEnd);
    }

    private static int findAvailablePort() throws IOException {
        try (ServerSocket socket = new ServerSocket()) {
            socket.bind(new InetSocketAddress(0));
            return socket.getLocalPort();
        }
    }

    private static String postJson(int port, String path, String body, String authHeader) throws Exception {
        HttpURLConnection connection = openConnection(port, path, "POST", authHeader);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        try (OutputStream os = connection.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        int status = connection.getResponseCode();
        String response = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (status >= 400) {
            throw new IllegalStateException("Unexpected status " + status + ": " + response);
        }
        return response;
    }

    private static int postJsonStatus(int port, String path, String body, String authHeader) throws Exception {
        HttpURLConnection connection = openConnection(port, path, "POST", authHeader);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        try (OutputStream os = connection.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        return connection.getResponseCode();
    }

    private static String getJson(int port, String path, String authHeader) throws Exception {
        HttpURLConnection connection = openConnection(port, path, "GET", authHeader);
        int status = connection.getResponseCode();
        String response = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (status >= 400) {
            throw new IllegalStateException("Unexpected status " + status + ": " + response);
        }
        return response;
    }

    private static HttpURLConnection openConnection(int port, String path, String method, String authHeader) throws Exception {
        URL url = new URL("http://127.0.0.1:" + port + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        if (authHeader != null) {
            connection.setRequestProperty("Authorization", authHeader);
        }
        return connection;
    }

    private static String basicAuth(String accountId, String password) {
        String token = accountId + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " (expected " + expected + ", got " + actual + ")");
        }
    }
}
