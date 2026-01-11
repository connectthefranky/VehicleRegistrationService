package repository;

import model.Registration;

public class RegistrationRepository {
    private final Database database;

    public RegistrationRepository(Database database) {
        this.database = database;
    }

    public boolean exists(String registrationCode) {
        return database.registrationExists(registrationCode);
    }

    public void save(Registration registration) {
        database.saveRegistration(registration.getRegistrationCode(), registration);
    }

    public Registration findByCode(String registrationCode) {
        return database.getRegistration(registrationCode);
    }
}
