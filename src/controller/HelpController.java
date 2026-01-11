package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import util.HttpUtils;
import util.PageLoader;

import java.io.IOException;

import static util.HttpUtils.isGetRequest;
import static util.HttpUtils.sendTextMethodNotAllowed;

public class HelpController implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (! isGetRequest(exchange)) {
            sendTextMethodNotAllowed(exchange);
            return;
        }

        String content = PageLoader.loadText("help.txt");
        HttpUtils.sendText(exchange, 200, content);
    }
}
