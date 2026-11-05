package ru.learning.taskapiservice.security;

import ru.learning.taskapiservice.exceptions.InvalidRequestException;

import java.nio.charset.StandardCharsets;

/** Правила для новых паролей (будут общими для смены пароля и работы администратора). */
public final class PasswordPolicy {

    /** BCrypt учитывает только первые 72 байта пароля. */
    private static final int MAX_BYTES = 72;

    private PasswordPolicy() {
    }

    public static void check(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw new InvalidRequestException("Password must not exceed 72 bytes");
        }
    }
}