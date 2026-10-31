package ru.learning.taskapiservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.learning.taskapiservice.entity.ProcessedMessage;

import java.util.UUID;

public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, UUID> {

    /** Занимает messageId. Возвращает 1, если сообщение новое, и 0, если оно уже обрабатывалось. */
    @Modifying
    @Query(value = "INSERT INTO processed_messages (message_id) VALUES (:id) ON CONFLICT (message_id) DO NOTHING",
            nativeQuery = true)
    int claim(@Param("id") UUID id);

    @Modifying
    @Query("update ProcessedMessage m set m.taskId = :taskId where m.messageId = :id")
    int attachTask(@Param("id") UUID id, @Param("taskId") Long taskId);
}