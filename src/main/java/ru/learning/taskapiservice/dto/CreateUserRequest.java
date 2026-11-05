package ru.learning.taskapiservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ru.learning.taskapiservice.entity.Role;

public record CreateUserRequest(
        @NotBlank(message = "must not be blank")
        @Pattern(regexp = "^[A-Za-z0-9._-]{3,50}$",
                message = "must be 3-50 characters: letters, digits, dot, underscore or hyphen")
        String username,

        @NotBlank(message = "must not be blank")
        @Size(min = 8, max = 72, message = "must be between 8 and 72 characters")
        String password,

        @NotNull(message = "must not be null")
        Role role) {
}