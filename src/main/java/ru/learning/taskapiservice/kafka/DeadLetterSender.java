package ru.learning.taskapiservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

/** Складывает необработанные сообщения из task_in в task_in_dlq вместе с причиной. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterSender {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;

    @Value("${app.kafka.topic-in-dlq}")
    private String topic;

    public void send(String originalValue, String error) {
        try {
            String json = jsonMapper.writeValueAsString(
                    new DeadLetterMessage(originalValue, error, Instant.now()));
            kafkaTemplate.send(topic, json).whenComplete((result, sendError) -> {
                if (sendError != null) {
                    log.error("Не удалось отправить сообщение в {}: {}", topic, error, sendError);
                }
            });
        } catch (RuntimeException e) {
            log.error("Не удалось подготовить сообщение для {}: {}", topic, error, e);
        }
    }
}