package ru.learning.taskapiservice.kafka;

import java.time.Instant;

/** Сообщение, которое не удалось обработать. Отправляется в топик task_in_dlq. */
public record DeadLetterMessage(
        String originalValue,
        String error,
        Instant failedAt
) {
}