package cz.pavel.taskmanagement.backend.controller;

import cz.pavel.taskmanagement.backend.dto.project.ProjectCreateDTO;
import cz.pavel.taskmanagement.backend.dto.project.ProjectResponseDTO;
import cz.pavel.taskmanagement.backend.dto.project.ProjectUpdateDTO;
import cz.pavel.taskmanagement.backend.security.SecurityUtils;
import cz.pavel.taskmanagement.backend.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Project controller", description = "Project management endpoints")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "Get all projects", description = "Retrieve a list of all projects in the system")
    public ResponseEntity<List<ProjectResponseDTO>> getAllProject() {
        log.info("GET /api/projects - Fetching all projects");
        List<ProjectResponseDTO> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("{id}")
    @Operation(summary = "Get project by ID", description = "Retrieve a specific project by its ID")
    public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable Long id) {
        log.info("GET /api/projects/{} - Fetching project by id: ", id);
        ProjectResponseDTO project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Get project by owner ID", description = "Retrieve a specific project by its owner")
    public ResponseEntity<List<ProjectResponseDTO>> getProjectsByOwnerId(@PathVariable Long ownerId) {
        log.info("GET /api/project/owner/{} - Fetching project by ownerId: ", ownerId);
        List<ProjectResponseDTO> project = projectService.getProjectsByOwner(ownerId);
        return ResponseEntity.ok(project);
    }

    @PostMapping
    @Operation(summary = "Create new project", description = "Create a new project in the system")
    public ResponseEntity<ProjectResponseDTO> createProject(
            @Valid @RequestBody ProjectCreateDTO projectCreateDTO,
            @RequestParam Long ownerId) {
        // Users can only create projects for themselves, admins can create for anyone
        if (!SecurityUtils.isAdminOrCurrentUser(ownerId)) {
            throw new AccessDeniedException("You can only create projects for yourself");
        }
        log.info("POST /api/projects - Creating new project for owner {}", ownerId);
        ProjectResponseDTO project = projectService.createProject(projectCreateDTO, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    @PutMapping("{id}")
    @Operation(summary = "Update project", description = "Update an existing project's information (owner or admin)")
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectUpdateDTO projectUpdateDTO) {
        ProjectResponseDTO existing = projectService.getProjectById(id);
        if (!SecurityUtils.isAdminOrCurrentUser(existing.getOwner().getId())) {
            throw new AccessDeniedException("You can only update your own projects");
        }
        log.info("PUT /api/projects - Updating project id {}", id);
        ProjectResponseDTO project = projectService.updateProject(id, projectUpdateDTO);
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Delete project", description = "Remove project from the system (owner or admin)")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        ProjectResponseDTO existing = projectService.getProjectById(id);
        if (!SecurityUtils.isAdminOrCurrentUser(existing.getOwner().getId())) {
            throw new AccessDeniedException("You can only delete your own projects");
        }
        log.info("DELETE /api/project - Deleting project id {}", id);
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
