package ru.learning.taskapiservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.learning.taskapiservice.dto.PatchTaskRequest;
import ru.learning.taskapiservice.dto.TaskStatus;
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
        task.setDescription(blankToNull(task.getDescription()));
        task.setAssignee(blankToNull(task.getAssignee()));
        return taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        taskRepository.delete(task);
    }

    @Transactional
    public Task updateTask(Long id, Task updatedTask) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(blankToNull(updatedTask.getDescription()));
        existingTask.setAssignee(blankToNull(updatedTask.getAssignee()));
        existingTask.setPriority(updatedTask.getPriority());
        existingTask.setStatus(updatedTask.getStatus());

        return taskRepository.save(existingTask);

    }

    @Transactional
    public Task patchTask(Long id, PatchTaskRequest request){
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (request.title() != null){task.setTitle(request.title());}
        if (request.description() != null){task.setDescription(request.description());}
        if (request.status() != null){task.setStatus(request.status());}
        if (request.priority() != null){task.setPriority(request.priority());}
        if (request.assignee() != null){task.setAssignee(request.assignee());}

        return taskRepository.save(task);
    }

    public List<Task> searchTasks(String q, TaskStatus status) {
        String pattern = (q == null || q.isBlank()) ? null : "%" + q.trim().toLowerCase() + "%";
        return taskRepository.search(pattern, status);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

}