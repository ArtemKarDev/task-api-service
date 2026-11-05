package ru.learning.taskapiservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";
    private static final String LOGIN_PATH = "/api/auth/login";

    @Bean
    public OpenAPI taskApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Task API")
                        .description("""
                                REST API for managing tasks.
                                Supports creating, retrieving, updating
                                and deleting tasks.

                                Authentication: get an access token with POST /api/auth/login
                                and pass it in the header "Authorization: Bearer <token>".
                                """)
                        .version("1.0.0"))
                .components(new Components().addSecuritySchemes(BEARER_AUTH,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }

    /** Добавляет ответ 401 в формате ApiErrorResponse ко всем методам, кроме входа. */
    @Bean
    public OpenApiCustomizer unauthorizedResponseCustomizer() {
        return openApi -> openApi.getPaths().forEach((path, item) -> {
            if (LOGIN_PATH.equals(path)) {
                return;
            }
            item.readOperations().forEach(operation -> operation.getResponses().addApiResponse("401",
                    new ApiResponse()
                            .description("Authentication required, or the token is invalid or expired")
                            .content(new Content().addMediaType("application/json",
                                    new MediaType().schema(new Schema<>().$ref("#/components/schemas/ApiErrorResponse"))))));
        });
    }
}