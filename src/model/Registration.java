package model;

import java.time.LocalDate;

public class Registration {
    private final String registrationCode;
    private final String accountId;
    private final LocalDate validUntil;

    public Registration(String registrationCode, String accountId, LocalDate validUntil) {
        this.registrationCode = registrationCode;
        this.accountId = accountId;
        this.validUntil = validUntil;
    }

    public String getRegistrationCode() {
        return registrationCode;
    }

    public String getAccountId() {
        return accountId;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }
}
