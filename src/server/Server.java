package server;

import com.sun.net.httpserver.HttpServer;
import controller.AccountController;
import controller.GuiController;
import controller.HelpController;
import controller.RegistrationController;
import controller.RegisterController;
import controller.StatisticsController;
import repository.AccountRepository;
import repository.Database;
import repository.RegistrationRepository;
import repository.StatisticsRepository;
import service.AccountService;
import service.AuthService;
import service.RegistrationService;
import service.StatisticsService;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {
    private static final int DEFAULT_PORT = 8089;

    public static void startServer(String[] args) throws IOException {
        startServer(DEFAULT_PORT);
    }

    public static HttpServer startServer(int port) throws IOException {
        Database database = new Database();

        AccountRepository accountRepository = new AccountRepository(database);
        RegistrationRepository registrationRepository = new RegistrationRepository(database);
        StatisticsRepository statisticsRepository = new StatisticsRepository(database);

        AccountService accountService = new AccountService(accountRepository, statisticsRepository);
        AuthService authService = new AuthService(accountRepository);
        RegistrationService registrationService = new RegistrationService(registrationRepository, statisticsRepository);
        StatisticsService statisticsService = new StatisticsService(statisticsRepository);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/account", new AccountController(accountService));
        server.createContext("/register", new RegisterController(authService, registrationService));
        server.createContext("/statistics", new StatisticsController(authService, statisticsService));
        server.createContext("/registration", new RegistrationController(registrationService));
        server.createContext("/help", new HelpController());
        server.createContext("/gui", new GuiController());
        server.setExecutor(null);

        server.start();
        System.out.println("Vehicle Registration Service started on port " + server.getAddress().getPort());

        return server;
    }
}
