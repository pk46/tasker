package cz.pavel.taskmanagement.backend.controller;

import cz.pavel.taskmanagement.backend.dto.project.ProjectResponseDTO;
import cz.pavel.taskmanagement.backend.dto.task.TaskCreateDTO;
import cz.pavel.taskmanagement.backend.dto.task.TaskResponseDTO;
import cz.pavel.taskmanagement.backend.dto.task.TaskUpdateDTO;
import cz.pavel.taskmanagement.backend.entity.TaskStatus;
import cz.pavel.taskmanagement.backend.security.SecurityUtils;
import cz.pavel.taskmanagement.backend.service.ProjectService;
import cz.pavel.taskmanagement.backend.service.TaskService;
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
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Task controller", description = "Task management endpoints")
public class TaskController {

    private final TaskService taskService;
    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "Get all tasks")
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks() {
      log.info("GET /api/tasks - Fetching all tasks");
      List<TaskResponseDTO> tasks = taskService.getAllTasks();
      return ResponseEntity.ok(tasks);
    }

    @GetMapping("{id}")
    @Operation(summary = "Get task by its id")
    public ResponseEntity<TaskResponseDTO> getTaskById(@PathVariable Long id) {
        log.info("GET /api/tasks/{} - Fetching task by id: ", id);
        TaskResponseDTO task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get tasks by project")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByProject(@PathVariable Long projectId) {
        log.info("GET /api/tasks/project/{} - Fetching tasks by project", projectId);
        List<TaskResponseDTO> tasks = taskService.getTasksByProject(projectId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/assignee/{assigneeId}")
    @Operation(summary = "Get tasks by assignee")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByAssignee(@PathVariable Long assigneeId) {
        log.info("GET /api/tasks/assignee/{} - Fetching tasks by assignee", assigneeId);
        List<TaskResponseDTO> tasks = taskService.getTasksByAssignee(assigneeId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get tasks by status")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByStatus(@PathVariable TaskStatus status) {
        log.info("GET /api/tasks/status/{} - Fetching tasks by status", status);
        List<TaskResponseDTO> tasks = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    @Operation(summary = "Create new task")
    public ResponseEntity<TaskResponseDTO> createTask(@Valid @RequestBody TaskCreateDTO createDTO) {
        // Verify user is project owner or admin
        ProjectResponseDTO project = projectService.getProjectById(createDTO.getProjectId());
        if (!SecurityUtils.isAdminOrCurrentUser(project.getOwner().getId())) {
            throw new AccessDeniedException("You can only create tasks in your own projects");
        }
        log.info("POST /api/tasks - Creating new task: {}", createDTO.getTitle());
        TaskResponseDTO createdTask = taskService.createTask(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task", description = "Update an existing task's information (project owner, assignee, or admin)")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskUpdateDTO updateDTO) {
        checkTaskAccess(id);
        log.info("PUT /api/tasks/{} - Updating task", id);
        TaskResponseDTO updatedTask = taskService.updateTask(id, updateDTO);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task", description = "Remove task from the system (project owner or admin)")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        // Only project owner or admin can delete
        TaskResponseDTO task = taskService.getTaskById(id);
        ProjectResponseDTO project = projectService.getProjectById(task.getProjectId());
        if (!SecurityUtils.isAdminOrCurrentUser(project.getOwner().getId())) {
            throw new AccessDeniedException("You can only delete tasks in your own projects");
        }
        log.info("DELETE /api/tasks/{} - Deleting task", id);
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    private void checkTaskAccess(Long taskId) {
        if (SecurityUtils.isAdmin()) return;

        TaskResponseDTO task = taskService.getTaskById(taskId);
        Long currentUserId = SecurityUtils.getCurrentUser().getUserId();

        // Allow if user is the assignee
        if (task.getAssignee() != null && currentUserId.equals(task.getAssignee().getId())) {
            return;
        }

        // Allow if user is the project owner
        ProjectResponseDTO project = projectService.getProjectById(task.getProjectId());
        if (currentUserId.equals(project.getOwner().getId())) {
            return;
        }

        throw new AccessDeniedException("You can only modify tasks assigned to you or in your own projects");
    }
}
