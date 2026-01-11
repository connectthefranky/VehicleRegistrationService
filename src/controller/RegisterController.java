package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.iface.IAuthService;
import service.iface.IRegistrationService;
import util.HttpUtils;
import util.JsonUtils;

import java.io.IOException;
import java.util.Map;

public class RegisterController implements HttpHandler {
    private final IAuthService authService;
    private final IRegistrationService registrationService;

    public RegisterController(IAuthService authService, IRegistrationService registrationService) {
        this.authService = authService;
        this.registrationService = registrationService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtils.sendJson(exchange, 405, JsonUtils.error("Method not allowed"));
            return;
        }
        String accountId = authService.authenticate(exchange.getRequestHeaders());
        if (accountId == null) {
            HttpUtils.sendJson(exchange, 401, JsonUtils.error("Unauthorized"));
            return;
        }
        String body = HttpUtils.readBody(exchange.getRequestBody());
        Map<String, String> payload = JsonUtils.parseJson(body);
        String registrationCode = payload.get("registrationCode");
        String validUntil = payload.get("validUntil");
        HttpUtils.sendJson(exchange, registrationService.registerVehicle(accountId, registrationCode, validUntil));
    }
}
