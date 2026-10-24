package ru.learning.taskapiservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskForm {

    @NotBlank(message = "Введите название задачи")
    @Size(max = 255, message = "Название не должно превышать 255 символов")
    private String title;

    @Size(max = 5000, message = "Описание не должно превышать 5000 символов")
    private String description;

    @NotBlank(message = "Укажите исполнителя")
    @Size(max = 30, message = "Имя исполнителя не должно превышать 30 символов")
    private String assignee;

    @NotNull(message = "Выберите приоритет")
    private Priority priority;

    @NotNull(message = "Выберите статус")
    private TaskStatus status;

}
