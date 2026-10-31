package ru.learning.taskapiservice.kafka;

import ru.learning.taskapiservice.dto.Priority;
import ru.learning.taskapiservice.dto.TaskStatus;

/** Команда «создать задачу», приходит в топик task_in. */
public record TaskCommand(
        String messageId,
        String title,
        String description,
        TaskStatus status,
        Priority priority,
        String assignee
) {
}