package ru.learning.taskapiservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest (

        @NotBlank(message = "Title must be blank")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        String description,

        @NotBlank(message = "Status not be blank")
        @Pattern(
                regexp = "NEW|IN_PROGRESS|DONE",
                message = "Status must be NEW, IN_PROGRESS or DONE"
        )
        String status,

        @NotBlank(message = "Priority must not be blank")
        @Pattern(
                regexp = "LOW|MEDIUM|HIGH",
                message = "Priority must be LOW, MEDIUM or HIGH"
        )
        String priority,

        @Size(max = 100, message = "Assignee must not exceed 100 characters")
        String assignee

){

}
