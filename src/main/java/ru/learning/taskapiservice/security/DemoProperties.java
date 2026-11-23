package ru.learning.taskapiservice.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Учебный пользователь для посетителей сайта. Его логин и пароль публикуются на странице «О проекте».
 * Включён, только если enabled=true и задан пароль.
 */
@ConfigurationProperties(prefix = "app.demo")
public record DemoProperties(
        boolean enabled,
        @DefaultValue("demo") String username,
        String password
) {
    public boolean active() {
        return enabled && password != null && !password.isBlank();
    }
}