CREATE TABLE processed_messages (
    message_id   UUID      PRIMARY KEY,
    task_id      BIGINT,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);