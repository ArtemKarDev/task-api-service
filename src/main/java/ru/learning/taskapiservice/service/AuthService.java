package ru.learning.taskapiservice.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.learning.taskapiservice.entity.UserAccount;
import ru.learning.taskapiservice.repository.UserAccountRepository;

import java.util.Optional;

@Service
public class AuthService {

    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String dummyHash;

    public AuthService(UserAccountRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.dummyHash = passwordEncoder.encode("not-a-real-password");
    }

    /** Возвращает пользователя, если логин и пароль верны и пользователь не отключён. */
    public Optional<UserAccount> authenticate(String username, String password) {
        Optional<UserAccount> user = repository.findByUsername(username);
        // Хеш сверяем всегда, даже если пользователя нет: ответ занимает одинаковое время
        String hash = user.map(UserAccount::getPasswordHash).orElse(dummyHash);
        boolean matches = passwordEncoder.matches(password, hash);
        return user.filter(u -> matches && u.isEnabled());
    }
}