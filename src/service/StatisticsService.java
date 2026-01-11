package service;

import model.ServiceResponse;
import repository.StatisticsRepository;
import util.JsonUtils;

public class StatisticsService {
    private final StatisticsRepository statisticsRepository;

    public StatisticsService(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    public ServiceResponse getStatistics() {
        String body = JsonUtils.mapToJson(statisticsRepository.getAll());
        return new ServiceResponse(200, body);
    }
}
