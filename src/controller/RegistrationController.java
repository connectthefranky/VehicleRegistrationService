package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.RegistrationService;
import util.HttpUtils;
import util.JsonUtils;

import java.io.IOException;
import java.net.URI;

import static util.HttpUtils.isGetRequest;
import static util.HttpUtils.sendJsonMethodNotAllowed;

public class RegistrationController implements HttpHandler {
    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @Override
    public void handle(HttpExchange exchange) {
        if (! isGetRequest(exchange)) {
            sendJsonMethodNotAllowed(exchange);
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
