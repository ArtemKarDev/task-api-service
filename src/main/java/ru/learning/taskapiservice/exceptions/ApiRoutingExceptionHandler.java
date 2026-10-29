package ru.learning.taskapiservice.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.learning.taskapiservice.dto.ApiErrorResponse;

import java.time.Instant;
import java.util.List;

/**
 * Ошибки, которые возникают до выбора контроллера: неизвестный адрес (404) и неверный метод (405).
 * Для адресов /api/** отвечаем единым форматом, для остальных (страницы сайта) оставляем обычное поведение.
 */
@RestControllerAdvice
public class ApiRoutingExceptionHandler {

    @ExceptionHandler({NoResourceFoundException.class, HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<ApiErrorResponse> handleRouting(Exception exception,
                                                          HttpServletRequest request) throws Exception {
        if (!request.getRequestURI().startsWith("/api/")) {
            // Тот же объект исключения: Spring без предупреждений передаёт его стандартному обработчику
            throw exception;
        }

        ErrorResponse errorResponse = (ErrorResponse) exception;
        HttpStatus status = HttpStatus.valueOf(errorResponse.getStatusCode().value());

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                status == HttpStatus.NOT_FOUND ? "Resource not found" : status.getReasonPhrase(),
                List.of(),
                request.getRequestURI());

        // errorResponse.getHeaders() содержит заголовок Allow для 405
        return ResponseEntity.status(status).headers(errorResponse.getHeaders()).body(body);
    }
}