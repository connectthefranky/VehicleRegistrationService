package repository.impl;

import model.Registration;
import repository.Database;
import repository.iface.IRegistrationRepository;

public class RegistrationRepository implements IRegistrationRepository {
    private final Database database;

    public RegistrationRepository(Database database) {
        this.database = database;
    }

    @Override
    public boolean exists(String registrationCode) {
        return database.registrationExists(registrationCode);
    }

    @Override
    public void save(Registration registration) {
        database.saveRegistration(registration.getRegistrationCode(), registration);
    }

    @Override
    public Registration findByCode(String registrationCode) {
        return database.getRegistration(registrationCode);
    }
}
