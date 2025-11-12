package cz.pavel.taskmanagement.backend.dto.project;

import cz.pavel.taskmanagement.backend.dto.user.UserResponseDTO;
import cz.pavel.taskmanagement.backend.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponseDTO {

    private Long id;
    private String name;
    private String description;
    private UserResponseDTO owner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int taskCount;

    public ProjectResponseDTO(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.description = project.getDescription();
        this.owner = new UserResponseDTO(project.getOwner());
        this.createdAt = project.getCreatedAt();
        this.updatedAt = project.getUpdatedAt();
    }
}