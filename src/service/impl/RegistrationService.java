package service.impl;

import model.Registration;
import model.ServiceResponse;
import repository.iface.IRegistrationRepository;
import repository.iface.IStatisticsRepository;
import service.iface.IRegistrationService;
import util.JsonUtils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class RegistrationService implements IRegistrationService {
    private final IRegistrationRepository registrationRepository;
    private final IStatisticsRepository statisticsRepository;

    public RegistrationService(IRegistrationRepository registrationRepository, IStatisticsRepository statisticsRepository) {
        this.registrationRepository = registrationRepository;
        this.statisticsRepository = statisticsRepository;
    }

    @Override
    public ServiceResponse registerVehicle(String accountId, String registrationCode, String validUntil) {
        if (registrationCode == null || registrationCode.isBlank() || validUntil == null || validUntil.isBlank()) {
            return new ServiceResponse(400, JsonUtils.error("registrationCode and validUntil are required"));
        }
        if (registrationRepository.exists(registrationCode)) {
            return new ServiceResponse(409, JsonUtils.error("Provided registration code already exists."));
        }
        LocalDate date;
        try {
            date = LocalDate.parse(validUntil);
        } catch (DateTimeParseException ex) {
            return new ServiceResponse(400, JsonUtils.error("validUntil must be ISO date (YYYY-MM-DD)"));
        }
        Registration registration = new Registration(registrationCode, accountId, date);
        registrationRepository.save(registration);
        statisticsRepository.increment(accountId);
        return new ServiceResponse(201, JsonUtils.successWithMessage("Vehicle registered successfully."));
    }

    @Override
    public ServiceResponse checkRegistration(String registrationCode) {
        Registration entry = registrationRepository.findByCode(registrationCode);
        if (entry == null) {
            return new ServiceResponse(404, JsonUtils.error("Registration not found"));
        }
        LocalDate today = LocalDate.now();
        String message = entry.getValidUntil().isBefore(today)
                ? "Your registration has expired."
                : "Your registration is still valid.";
        String body = String.format("{\"validUntil\":\"%s\",\"message\":\"%s\"}", entry.getValidUntil(), JsonUtils.escapeJson(message));
        return new ServiceResponse(200, body);
    }
}
