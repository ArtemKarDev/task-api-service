package ru.learning.taskapiservice.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ru.learning.taskapiservice.entity.Priority;
import ru.learning.taskapiservice.entity.TaskStatus;

public record PatchTaskRequest(

        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description,

//        @Pattern(regexp = "NEW|IN_PROGRESS|DONE")
        TaskStatus status,

//        @Pattern(regexp = "LOW|MEDIUM|HIGH")
        Priority priority,
        @Size(max = 100, message = "Assignee must not exceed 100 characters")
        String assignee
) {
        public boolean isEmpty(){
                return title == null
                        && description == null
                        && status == null
                        && priority == null
                        && assignee == null;

        }
}
