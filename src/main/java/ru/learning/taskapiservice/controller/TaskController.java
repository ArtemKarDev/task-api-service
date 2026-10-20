package ru.learning.taskapiservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.learning.taskapiservice.entity.Task;
import ru.learning.taskapiservice.service.TaskService;
import jakarta.validation.Valid;
import ru.learning.taskapiservice.dto.CreateTaskRequest;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public List<Task> getAllTask(){
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable("id") Long id){
        return taskService.getTaskById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task createTask(@Valid @RequestBody CreateTaskRequest request){

        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setAssignee(request.assignee());

        return taskService.createTask(task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable("id") Long id){
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();

    }

    @PutMapping("/{id}")
    public Task updateTask(
            @PathVariable("id") Long id,
            @Valid @RequestBody CreateTaskRequest request){
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setAssignee(request.assignee());
        task.setPriority(request.priority());

        return taskService.updateTask(id, task);
    }


}
