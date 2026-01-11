package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.AuthService;
import service.StatisticsService;
import util.HttpUtils;
import util.JsonUtils;

import java.io.IOException;

public class StatisticsController implements HttpHandler {
    private final AuthService authService;
    private final StatisticsService statisticsService;

    public StatisticsController(AuthService authService, StatisticsService statisticsService) {
        this.authService = authService;
        this.statisticsService = statisticsService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtils.sendJson(exchange, 405, JsonUtils.error("Method not allowed"));
            return;
        }
        String accountId = authService.authenticate(exchange.getRequestHeaders());
        if (accountId == null) {
            HttpUtils.sendJson(exchange, 401, JsonUtils.error("Unauthorized"));
            return;
        }
        HttpUtils.sendJson(exchange, statisticsService.getStatistics());
    }
}
