package service;

import model.ServiceResponse;

public interface IRegistrationService {
    ServiceResponse registerVehicle(String accountId, String registrationCode, String validUntil);

    ServiceResponse checkRegistration(String registrationCode);
}
