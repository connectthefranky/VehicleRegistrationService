package service.impl;

import model.Account;
import model.ServiceResponse;
import repository.iface.IAccountRepository;
import repository.iface.IStatisticsRepository;
import service.iface.IAccountService;
import util.JsonUtils;

import java.util.Random;

public class AccountService implements IAccountService {
    private static final int PASSWORD_LENGTH = 8;
    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private final IAccountRepository accountRepository;
    private final IStatisticsRepository statisticsRepository;
    private final Random random = new Random();

    public AccountService(IAccountRepository accountRepository, IStatisticsRepository statisticsRepository) {
        this.accountRepository = accountRepository;
        this.statisticsRepository = statisticsRepository;
    }

    @Override
    public ServiceResponse createAccount(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            return new ServiceResponse(400, JsonUtils.error("accountId is required"));
        }
        if (accountRepository.exists(accountId)) {
            return new ServiceResponse(409, JsonUtils.error("Provided account ID already exists."));
        }
        String password = generatePassword();
        accountRepository.save(new Account(accountId, password));
        statisticsRepository.initializeAccount(accountId);
        String body = String.format("{\"success\":true,\"description\":\"Your account has been created.\",\"password\":\"%s\"}", password);
        return new ServiceResponse(201, body);
    }

    private String generatePassword() {
        StringBuilder builder = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            builder.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        return builder.toString();
    }
}
