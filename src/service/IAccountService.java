package service;

import model.ServiceResponse;

public interface IAccountService {
    ServiceResponse createAccount(String accountId);
}
