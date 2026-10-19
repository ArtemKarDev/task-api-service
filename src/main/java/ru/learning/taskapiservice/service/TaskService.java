package ru.learning.taskapiservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.learning.taskapiservice.entity.Task;
import ru.learning.taskapiservice.repository.TaskRepository;
import ru.learning.taskapiservice.exceptions.TaskNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() ->
                        new TaskNotFoundException(id));
    }

    @Transactional
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }
}