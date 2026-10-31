package ru.learning.taskapiservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/** Обработанное сообщение из Kafka. Нужно, чтобы повторная доставка не создавала дубль задачи. */
@Entity
@Table(name = "processed_messages")
@Getter
@Setter
@NoArgsConstructor
public class ProcessedMessage {

    @Id
    @Column(name = "message_id")
    private UUID messageId;

    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;
}