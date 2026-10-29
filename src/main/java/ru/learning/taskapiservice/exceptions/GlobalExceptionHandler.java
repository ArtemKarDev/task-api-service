package ru.learning.taskapiservice.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.learning.taskapiservice.dto.ApiErrorResponse;
import ru.learning.taskapiservice.dto.FieldErrorDetail;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Единая обработка ошибок REST-контроллеров (веб-страницы Thymeleaf сюда не попадают).
 * В ответах нет стека и внутренних деталей: подробности пишутся только в лог.
 */
@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleTaskNotFound(TaskNotFoundException exception,
                                                               HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), List.of(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(MethodArgumentNotValidException exception,
                                                                   HttpServletRequest request) {
        List<FieldErrorDetail> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                .sorted(Comparator.comparing(FieldErrorDetail::field)
                        .thenComparing(FieldErrorDetail::message))
                .toList();

        return build(HttpStatus.BAD_REQUEST, "Validation failed", details, request);
    }

    /** Битый JSON, пустое тело, неверный тип поля, значение вне enum. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException exception,
                                                              HttpServletRequest request) {
        log.debug("Не удалось прочитать тело запроса: {}", exception.getMessage());
        return build(HttpStatus.BAD_REQUEST,
                "Request body is missing, malformed or contains an invalid value", List.of(), request);
    }

    /** Параметр адреса не того типа: /api/tasks/abc, слишком большое число. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception,
                                                               HttpServletRequest request) {
        String value = String.valueOf(exception.getValue());
        if (value.length() > 100) {
            value = value.substring(0, 100) + "…";
        }
        List<FieldErrorDetail> details = List.of(
                new FieldErrorDetail(exception.getName(), "invalid value '" + value + "'"));
        return build(HttpStatus.BAD_REQUEST, "Invalid request parameter", details, request);
    }

    /** Нарушение ограничений БД. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException exception,
                                                                HttpServletRequest request) {
        log.warn("Нарушение целостности данных на {} {}", request.getMethod(), request.getRequestURI(), exception);
        return build(HttpStatus.CONFLICT,
                "Data conflict: the request violates data integrity constraints", List.of(), request);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequest(InvalidRequestException exception,
                                                                 HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), List.of(), request);
    }

    /**
     * Всё остальное. Стандартные веб-ошибки Spring (415 и т.п.) реализуют ErrorResponse и получают
     * свой статус. Неизвестные ошибки: 500 без подробностей, стек только в логе.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAny(Exception exception, HttpServletRequest request) {
        if (exception instanceof ErrorResponse errorResponse) {
            HttpStatus status = HttpStatus.valueOf(errorResponse.getStatusCode().value());
            return build(status, status.getReasonPhrase(), List.of(), request);
        }
        log.error("Необработанная ошибка на {} {}", request.getMethod(), request.getRequestURI(), exception);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", List.of(), request);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status,
                                                   String message,
                                                   List<FieldErrorDetail> details,
                                                   HttpServletRequest request) {
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                details,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}