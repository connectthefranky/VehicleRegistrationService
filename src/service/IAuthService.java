package service;

import com.sun.net.httpserver.Headers;

public interface IAuthService {
    String authenticate(Headers headers);
}
