package ru.learning.taskapiservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

public record ValidationErrorResponse(

        @Schema(
                description = "Validation errors grouped by request field",
                example = "{\"title\":\"Title must not be blank\",\"status\":\"Status must be NEW, IN_PROGRESS or DONE\"}"
        )
        Map<String, String> errors
) {
}