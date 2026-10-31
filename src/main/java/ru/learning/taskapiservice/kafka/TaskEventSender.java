package ru.learning.taskapiservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.databind.json.JsonMapper;

/**
 * Отправляет события в Kafka только после успешной фиксации транзакции в БД.
 * Сбой отправки не влияет на ответ клиенту: ошибка пишется в лог.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskEventSender {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;

    @Value("${app.kafka.topic-out}")
    private String topic;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void send(TaskEvent event) {
        try {
            String json = jsonMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, String.valueOf(event.taskId()), json)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.error("Событие {} не отправлено в {}", event.eventId(), topic, error);
                        } else {
                            log.debug("Событие {} {} отправлено в {}, смещение {}",
                                    event.eventType(), event.taskId(), topic, result.getRecordMetadata().offset());
                        }
                    });
        } catch (RuntimeException e) {
            log.error("Не удалось подготовить событие {} к отправке", event.eventId(), e);
        }
    }
}