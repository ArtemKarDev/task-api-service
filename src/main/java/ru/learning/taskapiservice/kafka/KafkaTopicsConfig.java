package ru.learning.taskapiservice.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** Описание топиков. Приложение создаёт их при запуске, если их ещё нет. */
@Configuration
public class KafkaTopicsConfig {

    @Bean
    public NewTopic taskInTopic(@Value("${app.kafka.topic-in}") String name) {
        return TopicBuilder.name(name).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic taskInDlqTopic(@Value("${app.kafka.topic-in-dlq}") String name) {
        return TopicBuilder.name(name).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic taskOutTopic(@Value("${app.kafka.topic-out}") String name) {
        return TopicBuilder.name(name).partitions(1).replicas(1).build();
    }
}