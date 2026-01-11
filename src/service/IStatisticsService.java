package service;

import model.ServiceResponse;

public interface IStatisticsService {
    ServiceResponse getStatistics(String accountId);
}
