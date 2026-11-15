package ru.learning.taskapiservice.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.directory")
public record DirectoryProperties(
        String baseUrl,
        String tokenUrl,
        String clientId,
        String clientSecret,
        @DefaultValue("2s") Duration connectTimeout,
        @DefaultValue("3s") Duration readTimeout) {
}