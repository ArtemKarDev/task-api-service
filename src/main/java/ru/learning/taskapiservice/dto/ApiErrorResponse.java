package ru.learning.taskapiservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Единый формат ошибки API")
public record ApiErrorResponse(

        @Schema(type = "string", example = "2026-10-28T19:21:00.123Z", description = "Время ошибки (UTC)")
        Instant timestamp,

        @Schema(example = "400")
        int status,

        @Schema(example = "Bad Request")
        String error,

        @Schema(example = "Validation failed")
        String message,

        @Schema(description = "Ошибки по полям. Пустой список, если ошибка не связана с полями")
        List<FieldErrorDetail> details,

        @Schema(example = "/api/tasks")
        String path
) {
}
