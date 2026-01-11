package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.AccountService;
import util.HttpUtils;
import util.JsonUtils;

import java.io.IOException;
import java.util.Map;

import static util.HttpUtils.isPostRequest;
import static util.HttpUtils.sendJsonMethodNotAllowed;

public class AccountController implements HttpHandler {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (! isPostRequest(exchange)) {
            sendJsonMethodNotAllowed(exchange);
            return;
        }

        String body = HttpUtils.readBody(exchange.getRequestBody());
        Map<String, String> payload = JsonUtils.parseJson(body);
        String accountId = payload.get("accountId");
        HttpUtils.sendJson(exchange, accountService.createAccount(accountId));
    }
}
