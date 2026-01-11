package repository.iface;

import model.Account;

public interface IAccountRepository {
    boolean exists(String accountId);

    void save(Account account);

    String getPassword(String accountId);
}
