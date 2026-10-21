package ru.learning.taskapiservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Task API")
                        .description("""
                                REST API for managing tasks.
                                Supports creating, retrieving, updating
                                and deleting tasks.
                                """)
                        .version("1.0.0"));
    }
}