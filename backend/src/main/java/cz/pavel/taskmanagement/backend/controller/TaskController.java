package cz.pavel.taskmanagement.backend.controller;

import cz.pavel.taskmanagement.backend.dto.task.TaskCreateDTO;
import cz.pavel.taskmanagement.backend.dto.task.TaskResponseDTO;
import cz.pavel.taskmanagement.backend.dto.task.TaskUpdateDTO;
import cz.pavel.taskmanagement.backend.entity.TaskStatus;
import cz.pavel.taskmanagement.backend.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Task controller", description = "Task management endpoints")
public class TaskController {

    private final TaskService taskService;

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
        log.info("POST /api/tasks - Creating new task: {}", createDTO.getTitle());
        TaskResponseDTO createdTask = taskService.createTask(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task", description = "Update an existing task's information")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskUpdateDTO updateDTO) {
        log.info("PUT /api/tasks/{} - Updating task", id);
        TaskResponseDTO updatedTask = taskService.updateTask(id, updateDTO);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task", description = "Remove taks from the system")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        log.info("DELETE /api/tasks/{} - Deleting task", id);
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
