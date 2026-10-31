package ru.learning.taskapiservice.kafka;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.learning.taskapiservice.dto.CreateTaskRequest;
import ru.learning.taskapiservice.entity.Task;
import ru.learning.taskapiservice.service.TaskService;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Читает команды из task_in и создаёт задачи по тем же правилам, что и REST. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskCommandListener {

    static final String ERROR_UNREADABLE =
            "Message is not valid JSON or contains unknown fields or invalid values";
    static final String ERROR_MESSAGE_ID =
            "messageId is missing or is not a UUID";
    static final String ERROR_VALIDATION_PREFIX = "Validation failed: ";

    private final JsonMapper jsonMapper;
    private final Validator validator;
    private final TaskService taskService;
    private final DeadLetterSender deadLetterSender;

    @KafkaListener(topics = "${app.kafka.topic-in}")
    public void onMessage(String payload) {
        TaskCommand command;
        try {
            command = jsonMapper.readValue(payload, TaskCommand.class);
        } catch (RuntimeException e) {
            log.warn("Сообщение отклонено: {} ({})", ERROR_UNREADABLE, e.getMessage());
            deadLetterSender.send(payload, ERROR_UNREADABLE);
            return;
        }

        UUID messageId = parseMessageId(command);
        if (messageId == null) {
            log.warn("Сообщение отклонено: {}", ERROR_MESSAGE_ID);
            deadLetterSender.send(payload, ERROR_MESSAGE_ID);
            return;
        }

        CreateTaskRequest request = new CreateTaskRequest(
                command.title(), command.description(), command.status(),
                command.priority(), command.assignee());

        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String error = ERROR_VALIDATION_PREFIX + describe(violations);
            log.warn("Сообщение {} отклонено: {}", messageId, error);
            deadLetterSender.send(payload, error);
            return;
        }

        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setAssignee(request.assignee());

        Optional<Task> saved = taskService.createTaskFromMessage(task, messageId);
        if (saved.isPresent()) {
            log.info("Из сообщения {} создана задача {}", messageId, saved.get().getId());
        } else {
            log.info("Сообщение {} уже обработано, повтор пропущен", messageId);
        }
    }

    private UUID parseMessageId(TaskCommand command) {
        if (command == null || command.messageId() == null) {
            return null;
        }
        try {
            return UUID.fromString(command.messageId());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String describe(Set<ConstraintViolation<CreateTaskRequest>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .sorted()
                .collect(Collectors.joining("; "));
    }
}