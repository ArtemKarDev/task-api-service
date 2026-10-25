package ru.learning.taskapiservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.learning.taskapiservice.dto.TaskStatus;
import ru.learning.taskapiservice.entity.Task;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task,Long> {

    @Query("""
    select t from Task t
    where (:status is null or t.status = :status)
      and (:pattern is null
           or lower(t.title) like :pattern
           or lower(t.description) like :pattern
           or lower(t.assignee) like :pattern)
    order by t.id
    """)
    List<Task> search(@Param("pattern") String pattern, @Param("status") TaskStatus status);

}
