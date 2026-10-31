package ru.learning.taskapiservice.kafka;

import ru.learning.taskapiservice.entity.Task;

import java.time.Instant;
import java.util.UUID;

/** Событие об изменении задачи. Отправляется в топик task_out. */
public record TaskEvent(
        String eventId,
        TaskEventType eventType,
        Instant occurredAt,
        String correlationId,
        Long taskId,
        TaskSnapshot task
) {
    public static TaskEvent of(TaskEventType type, Task task, String correlationId) {
        return new TaskEvent(
                UUID.randomUUID().toString(),
                type,
                Instant.now(),
                correlationId,
                task.getId(),
                TaskSnapshot.from(task));
    }
}