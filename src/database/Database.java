package database;

import model.Registration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Database implements IDatabase {
    private final Map<String, String> accounts = new ConcurrentHashMap<>();
    private final Map<String, Registration> registrations = new ConcurrentHashMap<>();
    private final Map<String, Integer> statistics = new ConcurrentHashMap<>();

    public boolean accountExists(String accountId) {
        return accounts.containsKey(accountId);
    }

    public void saveAccount(String accountId, String password) {
        accounts.put(accountId, password);
    }

    public String getPassword(String accountId) {
        return accounts.get(accountId);
    }

    public void saveRegistration(String registrationCode, Registration registration) {
        registrations.put(registrationCode, registration);
    }

    public Registration getRegistration(String registrationCode) {
        return registrations.get(registrationCode);
    }

    public boolean registrationExists(String registrationCode) {
        return registrations.containsKey(registrationCode);
    }

    public void initStatistics(String accountId) {
        statistics.put(accountId, 0);
    }

    public void incrementStatistics(String accountId) {
        statistics.computeIfPresent(accountId, (key, value) -> value + 1);
    }

    public Map<String, Integer> getStatistics() {
        return statistics;
    }
}
