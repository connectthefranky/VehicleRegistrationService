package repository.impl;

import repository.Database;
import repository.iface.IStatisticsRepository;

import java.util.Map;

public class StatisticsRepository implements IStatisticsRepository {
    private final Database database;

    public StatisticsRepository(Database database) {
        this.database = database;
    }

    @Override
    public void initializeAccount(String accountId) {
        database.initStatistics(accountId);
    }

    @Override
    public void increment(String accountId) {
        database.incrementStatistics(accountId);
    }

    @Override
    public Map<String, Integer> getAll() {
        return database.getStatistics();
    }
}
