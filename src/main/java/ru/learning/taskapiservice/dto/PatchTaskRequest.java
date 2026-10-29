package ru.learning.taskapiservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Частичное обновление: поле, которого нет в запросе (или null), не меняется.
 * Очистить поле через PATCH нельзя, для этого используется PUT.
 */
public record PatchTaskRequest(


        @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank")
        @Size(max = 255, message = "must not exceed 255 characters")
        String title,

        @Size(max = 5000, message = "must not exceed 5000 characters")
        String description,

        TaskStatus status,

        Priority priority,

        @Size(max = 100, message = "must not exceed 100 characters")
        String assignee
) {

        // Служебный метод: без этих аннотаций Jackson и Swagger считают его свойством "empty"
        @JsonIgnore
        @Schema(hidden = true)
        public boolean isEmpty() {
                return title == null
                        && description == null
                        && status == null
                        && priority == null
                        && assignee == null;
        }
}