package repository.impl;

import model.Account;
import repository.Database;
import repository.iface.IAccountRepository;

public class AccountRepository implements IAccountRepository {
    private final Database database;

    public AccountRepository(Database database) {
        this.database = database;
    }

    @Override
    public boolean exists(String accountId) {
        return database.accountExists(accountId);
    }

    @Override
    public void save(Account account) {
        database.saveAccount(account.getAccountId(), account.getPassword());
    }

    @Override
    public String getPassword(String accountId) {
        return database.getPassword(accountId);
    }
}
