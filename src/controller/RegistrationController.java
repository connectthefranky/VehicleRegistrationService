package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.IRegistrationService;
import util.HttpUtils;
import util.JsonUtils;

import java.io.IOException;
import java.net.URI;

import static util.HttpUtils.isGetRequest;

public class RegistrationController implements HttpHandler {
    private final IRegistrationService registrationService;

    public RegistrationController(IRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!isGetRequest(exchange)) {
            HttpUtils.sendJson(exchange, 405, JsonUtils.error("Method not allowed"));
            return;
        }
        URI uri = exchange.getRequestURI();
        String[] parts = uri.getPath().split("/");
        if (parts.length < 3 || parts[2].isBlank()) {
            HttpUtils.sendJson(exchange, 400, JsonUtils.error("registrationCode is required in path"));
            return;
        }
        String registrationCode = parts[2];
        HttpUtils.sendJson(exchange, registrationService.checkRegistration(registrationCode));
    }
}
