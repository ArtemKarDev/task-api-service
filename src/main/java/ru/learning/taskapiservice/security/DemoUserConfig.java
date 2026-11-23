package ru.learning.taskapiservice.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.learning.taskapiservice.entity.Role;
import ru.learning.taskapiservice.service.UserService;

@Slf4j
@Configuration
@EnableConfigurationProperties(DemoProperties.class)
public class DemoUserConfig {

    /** При старте создаёт учебного пользователя с ролью USER или возвращает его в рабочее состояние. */
    @Bean
    public ApplicationRunner demoUserInitializer(DemoProperties demo, UserService userService) {
        return args -> {
            if (!demo.active()) {
                log.info("Учебный пользователь выключен");
                return;
            }
            userService.ensureUser(demo.username(), demo.password(), Role.USER);
            log.info("Учебный пользователь '{}' готов", demo.username());
        };
    }
}