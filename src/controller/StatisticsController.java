package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.AuthService;
import service.StatisticsService;
import util.HttpUtils;
import util.JsonUtils;

import java.io.IOException;

import static util.HttpUtils.*;

public class StatisticsController implements HttpHandler {
    private final AuthService authService;
    private final StatisticsService statisticsService;

    public StatisticsController(AuthService authService, StatisticsService statisticsService) {
        this.authService = authService;
        this.statisticsService = statisticsService;
    }

    @Override
    public void handle(HttpExchange exchange) {
        if (! isGetRequest(exchange)) {
            sendJsonMethodNotAllowed(exchange);
            return;
        }

        String accountId = authService.authenticate(exchange.getRequestHeaders());
        if (accountId == null) {
            sendJsonUnauthorized(exchange);
        } else {
            sendJson(exchange, statisticsService.getStatistics());
        }
    }
}
