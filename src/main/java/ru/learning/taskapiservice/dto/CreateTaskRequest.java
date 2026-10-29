package ru.learning.taskapiservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(

        @NotBlank(message = "must not be blank")
        @Size(max = 255, message = "must not exceed 255 characters")
        String title,

        @Size(max = 5000, message = "must not exceed 5000 characters")
        String description,

        @NotNull(message = "must not be null")
        @Schema(description = "Task status", example = "NEW")
        TaskStatus status,

        @NotNull(message = "must not be null")
        @Schema(description = "Task priority", example = "HIGH")
        Priority priority,

        @Size(max = 100, message = "must not exceed 100 characters")
        String assignee

) {
}