package ru.learning.taskapiservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.learning.taskapiservice.entity.Role;
import ru.learning.taskapiservice.entity.UserAccount;
import ru.learning.taskapiservice.exceptions.*;
import ru.learning.taskapiservice.repository.UserAccountRepository;
import ru.learning.taskapiservice.security.PasswordPolicy;

import java.util.List;

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
            throw new PasswordChangeException(PasswordChangeException.Reason.WRONG_CURRENT,
                    "Current password is incorrect");
        }
        PasswordPolicy.check(newPassword);
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new PasswordChangeException(PasswordChangeException.Reason.SAME_AS_CURRENT,
                    "New password must differ from the current one");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        repository.save(user);
    }

    @Transactional
    public UserAccount createUser(String username, String rawPassword, Role role) {
        if (repository.existsByUsername(username)) {
            throw new UserAlreadyExistsException();
        }
        PasswordPolicy.check(rawPassword);

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return repository.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserAccount> listUsers() {
        return repository.findAll(Sort.by("id"));
    }

    @Transactional
    public UserAccount updateUser(Long id, Boolean enabled, Role role, String actorUsername) {
        if (enabled == null && role == null) {
            throw new InvalidRequestException("At least one of 'enabled' or 'role' must be provided");
        }
        UserAccount user = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        boolean self = user.getUsername().equals(actorUsername);
        if (self && (Boolean.FALSE.equals(enabled) || (role != null && role != Role.ADMIN))) {
            throw new InvalidRequestException("You cannot disable or demote your own account");
        }

        if (enabled != null) {
            user.setEnabled(enabled);
        }
        if (role != null) {
            user.setRole(role);
        }
        return repository.save(user);
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        UserAccount user = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        PasswordPolicy.check(newPassword);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        repository.save(user);
    }

    @Transactional
    public void deleteUser(Long id, String actorUsername) {
        UserAccount user = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        if (user.getUsername().equals(actorUsername)) {
            throw new InvalidRequestException("You cannot delete your own account");
        }
        repository.delete(user);
    }

}