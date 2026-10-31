package ru.learning.taskapiservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.learning.taskapiservice.dto.PatchTaskRequest;
import ru.learning.taskapiservice.dto.TaskStatus;
import ru.learning.taskapiservice.entity.Task;
import ru.learning.taskapiservice.exceptions.TaskNotFoundException;
import ru.learning.taskapiservice.kafka.TaskEvent;
import ru.learning.taskapiservice.kafka.TaskEventType;
import ru.learning.taskapiservice.repository.TaskRepository;
import ru.learning.taskapiservice.repository.ProcessedMessageRepository;
import java.util.Optional;
import java.util.UUID;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ProcessedMessageRepository processedMessageRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @Transactional
    public Task createTask(Task task) {
        return createTask(task, null);
    }

    /** correlationId: идентификатор команды из Kafka, если задача создана ею; иначе null. */
    @Transactional
    public Task createTask(Task task, String correlationId) {
        task.setDescription(blankToNull(task.getDescription()));
        task.setAssignee(blankToNull(task.getAssignee()));
        Task saved = taskRepository.save(task);
        publish(TaskEventType.TASK_CREATED, saved, correlationId);
        return saved;
    }

    /**
     * Создаёт задачу из сообщения Kafka ровно один раз на messageId.
     * Для уже обработанного messageId возвращает пустой результат и ничего не создаёт.
     */
    @Transactional
    public Optional<Task> createTaskFromMessage(Task task, UUID messageId) {
        if (processedMessageRepository.claim(messageId) == 0) {
            return Optional.empty();
        }
        Task saved = createTask(task, messageId.toString());
        processedMessageRepository.attachTask(messageId, saved.getId());
        return Optional.of(saved);
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        taskRepository.delete(task);
        publish(TaskEventType.TASK_DELETED, task, null);
    }

    @Transactional
    public Task updateTask(Long id, Task updatedTask) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(blankToNull(updatedTask.getDescription()));
        existingTask.setPriority(updatedTask.getPriority());
        existingTask.setStatus(updatedTask.getStatus());
        existingTask.setAssignee(blankToNull(updatedTask.getAssignee()));

        // saveAndFlush: updatedAt проставляется при записи в БД, а событие должно содержать актуальное время
        Task saved = taskRepository.saveAndFlush(existingTask);
        publish(TaskEventType.TASK_UPDATED, saved, null);
        return saved;
    }

    @Transactional
    public Task patchTask(Long id, PatchTaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (request.title() != null) {
            task.setTitle(request.title());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        if (request.assignee() != null) {
            task.setAssignee(request.assignee());
        }

        Task saved = taskRepository.saveAndFlush(task);
        publish(TaskEventType.TASK_UPDATED, saved, null);
        return saved;
    }

    public List<Task> searchTasks(String q, TaskStatus status) {
        String pattern = (q == null || q.isBlank()) ? null : "%" + q.trim().toLowerCase() + "%";
        return taskRepository.search(pattern, status);
    }

    /** Событие уйдёт в Kafka только после успешной фиксации транзакции (см. TaskEventSender). */
    private void publish(TaskEventType type, Task task, String correlationId) {
        eventPublisher.publishEvent(TaskEvent.of(type, task, correlationId));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}