package ru.learning.taskapiservice.controller;

import io.swagger.v3.oas.annotations.media.ExampleObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.learning.taskapiservice.dto.PatchTaskRequest;
import ru.learning.taskapiservice.entity.Task;
import ru.learning.taskapiservice.service.TaskService;
import jakarta.validation.Valid;
import ru.learning.taskapiservice.dto.CreateTaskRequest;
import ru.learning.taskapiservice.dto.ApiErrorResponse;
import ru.learning.taskapiservice.dto.ValidationErrorResponse;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@Tag(
        name = "Tasks",
        description = "API for managing tasks"
)
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public List<Task> getAllTask() {
        return taskService.getAllTasks();
    }


    @Operation(
            summary = "Get a task by ID",
            description = "Returns a task with the specified ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Task found",
                    content = @Content(
                            schema = @Schema(implementation = Task.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            ),
                            examples = @ExampleObject(value = """
                                    {
                                      "error": "Task not found with id: 123"
                                    }
                                    """)
                    )
            )
    })
    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable("id") Long id) {
        return taskService.getTaskById(id);
    }

    @Operation(
            summary = "Create a task",
            description = "Creates a new task and returns the created task"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Task successfully created",
                    content = @Content(
                            schema = @Schema(implementation = Task.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ValidationErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task createTask(@Valid @RequestBody CreateTaskRequest request) {

        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setAssignee(request.assignee());

        return taskService.createTask(task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable("id") Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();

    }

    @PutMapping("/{id}")
    public Task updateTask(
            @PathVariable("id") Long id,
            @Valid @RequestBody CreateTaskRequest request) {
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setAssignee(request.assignee());
        task.setPriority(request.priority());

        return taskService.updateTask(id, task);
    }


    @PatchMapping("/{id}")
    public Task patchTask(@PathVariable("id") Long id,
                          @Valid @RequestBody PatchTaskRequest request) {
        if (request.isEmpty()) {
            throw new IllegalArgumentException("At least one field must be provided");
        }

        if (request.title() != null && request.title().isBlank()) {
            throw new IllegalArgumentException("Title must be blank");
        }
        return taskService.patchTask(id, request);
    }
}
