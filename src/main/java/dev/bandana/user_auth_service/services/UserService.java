package dev.bandana.user_auth_service.services;

import dev.bandana.user_auth_service.models.Token;
import dev.bandana.user_auth_service.models.User;

public interface UserService {
    Token login(String email, String password);
    User signup(String name, String email, String password);
    User validateToken(String tokenValue);
    void logout(String tokenvalue);
}
