package repository;

import java.util.Map;

public class StatisticsRepository {
    private final Database database;

    public StatisticsRepository(Database database) {
        this.database = database;
    }

    public void initializeAccount(String accountId) {
        database.initStatistics(accountId);
    }

    public void increment(String accountId) {
        database.incrementStatistics(accountId);
    }

    public Map<String, Integer> getAll() {
        return database.getStatistics();
    }
}
