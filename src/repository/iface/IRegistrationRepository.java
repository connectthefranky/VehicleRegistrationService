package repository.iface;

import model.Registration;

public interface IRegistrationRepository {
    boolean exists(String registrationCode);

    void save(Registration registration);

    Registration findByCode(String registrationCode);
}
