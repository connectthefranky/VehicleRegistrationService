package database;

import model.Registration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface IDatabase {
    public boolean accountExists(String accountId);

    public void saveAccount(String accountId, String password);

    public String getPassword(String accountId);

    public void saveRegistration(String registrationCode, Registration registration);

    public Registration getRegistration(String registrationCode);

    public boolean registrationExists(String registrationCode);

    public void initStatistics(String accountId);

    public void incrementStatistics(String accountId);

    public Map<String, Integer> getStatistics();
}