package ru.learning.taskapiservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(

        @NotBlank(message = "Title must be blank")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description,

//        @NotBlank(message = "Status not be blank")
//        @Pattern(
//                regexp = "NEW|IN_PROGRESS|DONE",
//                message = "Status must be NEW, IN_PROGRESS or DONE"
//        )
        @NotNull(message = "Status not be null")
        @Schema(description = "Task status", example = "NEW")
        TaskStatus status,

//        @NotBlank(message = "Priority must not be blank")
//        @Pattern(
//                regexp = "LOW|MEDIUM|HIGH",
//                message = "Priority must be LOW, MEDIUM or HIGH"
//        )
        @NotNull(message = "Priority not be null")
        @Schema(description = "Task priority", example = "HIGH")
        Priority priority,

        @Size(max = 100, message = "Assignee must not exceed 100 characters")
        String assignee

) {

}
