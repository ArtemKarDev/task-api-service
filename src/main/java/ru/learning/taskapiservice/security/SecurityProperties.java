package ru.learning.taskapiservice.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(Account user, Account admin, Jwt jwt) {

    public record Account(String username, String password) {
    }

    public record Jwt(String secret, long ttlMinutes) {
    }
}