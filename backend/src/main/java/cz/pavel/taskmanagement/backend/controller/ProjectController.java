package cz.pavel.taskmanagement.backend.controller;

import cz.pavel.taskmanagement.backend.dto.project.ProjectCreateDTO;
import cz.pavel.taskmanagement.backend.dto.project.ProjectResponseDTO;
import cz.pavel.taskmanagement.backend.dto.project.ProjectUpdateDTO;
import cz.pavel.taskmanagement.backend.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> gettAllProject() {
        log.info("GET /api/projects - Fetching all projects");
        List<ProjectResponseDTO> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("{id}")
    public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable Long id) {
        log.info("GET /api/projects/{} - Fetching project by id: ", id);
        ProjectResponseDTO project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<ProjectResponseDTO>> getProjectsByOwnerId(@PathVariable Long ownerId) {
        log.info("GET /api/project/owner/{} - Fetching project by ownerId: ", ownerId);
        List<ProjectResponseDTO> project = projectService.getProjectsByOwner(ownerId);
        return ResponseEntity.ok(project);
    }

    @PostMapping
    public ResponseEntity<ProjectResponseDTO> createProject(
            @Valid @RequestBody ProjectCreateDTO projectCreateDTO,
            @RequestParam Long ownerId) {
        log.info("POST /api/projects - Creating new project for owner {}", ownerId);
        ProjectResponseDTO project = projectService.createProject(projectCreateDTO, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    @PutMapping("{id}")
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectUpdateDTO projectUpdateDTO) {
        log.info("PUT /api/projects - Updating project id {}", id);
        ProjectResponseDTO project = projectService.updateProject(id, projectUpdateDTO);
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        log.info("DELETE /api/project - Deleting project id {}", id);
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
