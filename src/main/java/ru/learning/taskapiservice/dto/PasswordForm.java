package ru.learning.taskapiservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordForm {

    @NotBlank(message = "Введите текущий пароль")
    private String currentPassword;

    @NotBlank(message = "Введите новый пароль")
    @Size(min = 8, max = 72, message = "Новый пароль: от 8 до 72 символов")
    private String newPassword;

    @NotBlank(message = "Повторите новый пароль")
    private String confirmPassword;
}