package service.impl;

import com.sun.net.httpserver.Headers;
import repository.iface.IAccountRepository;
import service.iface.IAuthService;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AuthService implements IAuthService {
    private final IAccountRepository accountRepository;

    public AuthService(IAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public String authenticate(Headers headers) {
        String authHeader = headers.getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return null;
        }
        String encoded = authHeader.substring("Basic ".length());
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return null;
        }
        int index = decoded.indexOf(':');
        if (index < 0) {
            return null;
        }
        String accountId = decoded.substring(0, index);
        String password = decoded.substring(index + 1);
        String stored = accountRepository.getPassword(accountId);
        if (stored == null || !stored.equals(password)) {
            return null;
        }
        return accountId;
    }
}
