package repository;

import java.util.Map;

public interface IStatisticsRepository {
    void initializeAccount(String accountId);

    void increment(String accountId);

    Map<String, Integer> getAll();
}
