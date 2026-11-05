package ru.learning.taskapiservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.learning.taskapiservice.entity.UserAccount;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsername(String username);

    boolean existsByUsername(String username);
}