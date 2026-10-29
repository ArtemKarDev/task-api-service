package ru.learning.taskapiservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.learning.taskapiservice.dto.ApiErrorResponse;
import ru.learning.taskapiservice.dto.CreateTaskRequest;
import ru.learning.taskapiservice.dto.PatchTaskRequest;
import ru.learning.taskapiservice.entity.Task;
import ru.learning.taskapiservice.exceptions.InvalidRequestException;
import ru.learning.taskapiservice.service.TaskService;

import java.util.List;

@Tag(name = "Tasks", description = "API for managing tasks")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Get all tasks", description = "Returns all tasks")
    @ApiResponse(responseCode = "200", description = "List of tasks")
    @GetMapping
    public List<Task> getAllTask() {
        return taskService.getAllTasks();
    }

    @Operation(summary = "Get a task by ID", description = "Returns a task with the specified ID")
    @ApiResponse(responseCode = "200", description = "Task found")
    @ApiResponse(responseCode = "400", description = "ID is not a valid number",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Task not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable("id") Long id) {
        return taskService.getTaskById(id);
    }

    @Operation(summary = "Create a task", description = "Creates a new task and returns the created task")
    @ApiResponse(responseCode = "201", description = "Task successfully created")
    @ApiResponse(responseCode = "400", description = "Request validation failed or body is malformed",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task createTask(@Valid @RequestBody CreateTaskRequest request) {
        return taskService.createTask(toTask(request));
    }

    @Operation(summary = "Replace a task",
            description = "Replaces all fields of the task. Optional fields that are omitted or blank become empty")
    @ApiResponse(responseCode = "200", description = "Task updated")
    @ApiResponse(responseCode = "400", description = "Request validation failed or body is malformed",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Task not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PutMapping("/{id}")
    public Task updateTask(@PathVariable("id") Long id,
                           @Valid @RequestBody CreateTaskRequest request) {
        return taskService.updateTask(id, toTask(request));
    }

    @Operation(summary = "Partially update a task",
            description = "Changes only the provided fields. A missing or null field is left unchanged; "
                    + "a field cannot be cleared with PATCH (use PUT). At least one field is required")
    @ApiResponse(responseCode = "200", description = "Task updated")
    @ApiResponse(responseCode = "400", description = "Request validation failed, body is malformed or empty",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Task not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PatchMapping("/{id}")
    public Task patchTask(@PathVariable("id") Long id,
                          @Valid @RequestBody PatchTaskRequest request) {
        if (request.isEmpty()) {
            throw new InvalidRequestException("At least one field must be provided");
        }
        return taskService.patchTask(id, request);
    }

    @Operation(summary = "Delete a task", description = "Deletes the task with the specified ID")
    @ApiResponse(responseCode = "204", description = "Task deleted")
    @ApiResponse(responseCode = "400", description = "ID is not a valid number",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Task not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable("id") Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    private Task toTask(CreateTaskRequest request) {
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setAssignee(request.assignee());
        return task;
    }
}