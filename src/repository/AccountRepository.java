package repository;

import model.Account;

public class AccountRepository {
    private final Database database;

    public AccountRepository(Database database) {
        this.database = database;
    }

    public boolean exists(String accountId) {
        return database.accountExists(accountId);
    }

    public void save(Account account) {
        database.saveAccount(account.getAccountId(), account.getPassword());
    }

    public String getPassword(String accountId) {
        return database.getPassword(accountId);
    }
}
