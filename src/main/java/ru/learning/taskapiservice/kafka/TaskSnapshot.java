package ru.learning.taskapiservice.kafka;

import ru.learning.taskapiservice.dto.Priority;
import ru.learning.taskapiservice.dto.TaskStatus;
import ru.learning.taskapiservice.entity.Task;

import java.time.LocalDateTime;

/** Состояние задачи на момент события. */
public record TaskSnapshot(
        Long id,
        String title,
        String description,
        TaskStatus status,
        Priority priority,
        String assignee,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TaskSnapshot from(Task task) {
        return new TaskSnapshot(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getAssignee(),
                task.getCreatedAt(),
                task.getUpdatedAt());
    }
}