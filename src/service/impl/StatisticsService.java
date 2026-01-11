package service.impl;

import model.ServiceResponse;
import repository.iface.IStatisticsRepository;
import service.iface.IStatisticsService;
import util.JsonUtils;

public class StatisticsService implements IStatisticsService {
    private final IStatisticsRepository statisticsRepository;

    public StatisticsService(IStatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    @Override
    public ServiceResponse getStatistics(String accountId) {
        Integer count = statisticsRepository.getAll().get(accountId);
        if (count == null) {
            count = 0;
        }
        String body = JsonUtils.mapToJson(java.util.Map.of(accountId, count));
        return new ServiceResponse(200, body);
    }
}
