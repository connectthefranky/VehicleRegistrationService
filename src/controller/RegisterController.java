package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.AuthService;
import service.RegistrationService;
import util.HttpUtils;
import util.JsonUtils;

import java.io.IOException;
import java.util.Map;

import static util.HttpUtils.*;

public class RegisterController implements HttpHandler {
    private final AuthService authService;
    private final RegistrationService registrationService;

    public RegisterController(AuthService authService, RegistrationService registrationService) {
        this.authService = authService;
        this.registrationService = registrationService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (! isPostRequest(exchange)) {
            sendJsonMethodNotAllowed(exchange);
            return;
        }

        String accountId = authService.authenticate(exchange.getRequestHeaders());
        if (accountId == null) {
            sendJsonUnauthorized(exchange);
            return;
        }
        String body = HttpUtils.readBody(exchange.getRequestBody());
        Map<String, String> payload = JsonUtils.parseJson(body);
        String registrationCode = payload.get("registrationCode");
        String validUntil = payload.get("validUntil");
        HttpUtils.sendJson(exchange, registrationService.registerVehicle(accountId, registrationCode, validUntil));
    }
}
