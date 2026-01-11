package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import util.HttpUtils;
import util.PageLoader;

import java.io.IOException;

public class GuiController implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtils.sendText(exchange, 405, "Method not allowed");
            return;
        }
        String content = PageLoader.loadHtml("gui.html");
        HttpUtils.sendHtml(exchange, 200, content);
    }
}
