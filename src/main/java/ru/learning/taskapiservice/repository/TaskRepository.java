package ru.learning.taskapiservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.learning.taskapiservice.entity.Task;

public interface TaskRepository extends JpaRepository<Task,Long> {

}
