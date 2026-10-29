package ru.learning.taskapiservice.dto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ошибка по конкретному полю запроса")
public record FieldErrorDetail(
        @Schema(example = "title") String field,
        @Schema(example = "must not be blank") String message
) {

}
