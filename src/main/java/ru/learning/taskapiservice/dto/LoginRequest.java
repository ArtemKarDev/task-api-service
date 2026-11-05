package ru.learning.taskapiservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "must not be blank")
        @Size(max = 100, message = "must not exceed 100 characters")
        String username,

        @NotBlank(message = "must not be blank")
        @Size(max = 100, message = "must not exceed 100 characters")
        String password) {
}