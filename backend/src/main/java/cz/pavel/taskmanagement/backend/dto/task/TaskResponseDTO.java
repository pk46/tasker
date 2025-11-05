package cz.pavel.taskmanagement.backend.dto.task;

import cz.pavel.taskmanagement.backend.dto.user.UserResponseDTO;
import cz.pavel.taskmanagement.backend.entity.Priority;
import cz.pavel.taskmanagement.backend.entity.Task;
import cz.pavel.taskmanagement.backend.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDTO {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private LocalDate dueDate;
    private Long projectId;
    private String projectName;
    private UserResponseDTO assignee;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TaskResponseDTO(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.status = task.getStatus();
        this.priority = task.getPriority();
        this.dueDate = task.getDueDate();
        this.projectId = task.getProject().getId();
        this.projectName = task.getProject().getName();
        this.assignee = task.getAssignee() != null ? new UserResponseDTO(task.getAssignee()) : null;
        this.createdAt = task.getCreatedAt();
        this.updatedAt = task.getUpdatedAt();
    }
}