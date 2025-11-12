package cz.pavel.taskmanagement.backend.repository;

import cz.pavel.taskmanagement.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProject(Project project);
    List<Task> findByAssignee(User assignee);
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByPriority(Priority priority);
    List<Task> findByProjectAndStatus(Project project, TaskStatus status);
    List<Task> findByAssigneeAndStatus(User assignee, TaskStatus status);
    List<Task> findByDueDateBefore(LocalDate date);
    long countByProject(Project project);

    @Query("SELECT t FROM Task t WHERE t.project.owner = :owner")
    List<Task> findAllTasksByProjectOwner(User owner);
}
