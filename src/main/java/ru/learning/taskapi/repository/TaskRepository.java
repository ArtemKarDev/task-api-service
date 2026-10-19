package ru.learning.taskapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.learning.taskapi.entity.Task;

public interface TaskRepository extends JpaRepository<Task,Long> {

}
