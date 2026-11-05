package ru.learning.taskapiservice.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.learning.taskapiservice.entity.Role;
import ru.learning.taskapiservice.entity.UserAccount;
import ru.learning.taskapiservice.repository.UserAccountRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSeeder implements ApplicationRunner {

    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        ensureUser(properties.user(), Role.USER);
        ensureUser(properties.admin(), Role.ADMIN);
    }

    private void ensureUser(SecurityProperties.Account account, Role role) {
        if (account == null || isBlank(account.username()) || isBlank(account.password())) {
            throw new IllegalStateException("Не заданы логин или пароль пользователя с ролью " + role
                    + ". Задайте переменные окружения APP_SECURITY_" + role + "_USERNAME и APP_SECURITY_" + role + "_PASSWORD");
        }

        var existing = repository.findByUsername(account.username());
        if (existing.isEmpty()) {
            UserAccount user = new UserAccount();
            user.setUsername(account.username());
            user.setPasswordHash(passwordEncoder.encode(account.password()));
            user.setRole(role);
            repository.save(user);
            log.info("Создан пользователь {} с ролью {}", account.username(), role);
        } else if (!passwordEncoder.matches(account.password(), existing.get().getPasswordHash())) {
            UserAccount user = existing.get();
            user.setPasswordHash(passwordEncoder.encode(account.password()));
            repository.save(user);
            log.info("Обновлён пароль пользователя {}", account.username());
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}