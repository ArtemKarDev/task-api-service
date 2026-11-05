package ru.learning.taskapiservice.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import ru.learning.taskapiservice.dto.ApiErrorResponse;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

/** Пишет ошибку в единый формат ApiErrorResponse прямо в ответ (для фильтров безопасности). */
@Component
@RequiredArgsConstructor
public class ApiErrorWriter {

    private final JsonMapper jsonMapper;

    public void write(HttpServletRequest request,
                      HttpServletResponse response,
                      HttpStatus status,
                      String message) throws IOException {
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                List.of(),
                request.getRequestURI());

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(jsonMapper.writeValueAsString(body));
    }
}