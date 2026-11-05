package ru.learning.taskapiservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.learning.taskapiservice.entity.UserAccount;
import ru.learning.taskapiservice.exceptions.InvalidCredentialsException;
import ru.learning.taskapiservice.exceptions.InvalidRequestException;
import ru.learning.taskapiservice.repository.UserAccountRepository;
import ru.learning.taskapiservice.security.PasswordPolicy;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        UserAccount user = repository.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidRequestException("Current password is incorrect");
        }
        PasswordPolicy.check(newPassword);
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new InvalidRequestException("New password must differ from the current one");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        repository.save(user);
    }
}