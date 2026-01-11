package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.iface.IAccountService;
import util.HttpUtils;
import util.JsonUtils;

import java.io.IOException;
import java.util.Map;

public class AccountController implements HttpHandler {
    private final IAccountService accountService;

    public AccountController(IAccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtils.sendJson(exchange, 405, JsonUtils.error("Method not allowed"));
            return;
        }
        String body = HttpUtils.readBody(exchange.getRequestBody());
        Map<String, String> payload = JsonUtils.parseJson(body);
        String accountId = payload.get("accountId");
        HttpUtils.sendJson(exchange, accountService.createAccount(accountId));
    }
}
