package ru.learning.taskapiservice.dto;

import ru.learning.taskapiservice.entity.Role;

/** Оба поля необязательны: меняется только то, что указано. */
public record UpdateUserRequest(Boolean enabled, Role role) {
}