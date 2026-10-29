package ru.learning.taskapiservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Для REST ответ формирует GlobalExceptionHandler; эта аннотация нужна веб-страницам (/tasks/{id}/edit):
// для них будет обычная страница 404, а не JSON
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(Long id) {
        super("Task not found with id: " + id);
    }
}