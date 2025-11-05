package cz.pavel.taskmanagement.backend.service;

import cz.pavel.taskmanagement.backend.dto.task.TaskCreateDTO;
import cz.pavel.taskmanagement.backend.dto.task.TaskResponseDTO;
import cz.pavel.taskmanagement.backend.dto.task.TaskUpdateDTO;
import cz.pavel.taskmanagement.backend.entity.Project;
import cz.pavel.taskmanagement.backend.entity.Task;
import cz.pavel.taskmanagement.backend.entity.TaskStatus;
import cz.pavel.taskmanagement.backend.entity.User;
import cz.pavel.taskmanagement.backend.exception.ResourceNotFoundException;
import cz.pavel.taskmanagement.backend.repository.ProjectRepository;
import cz.pavel.taskmanagement.backend.repository.TaskRepository;
import cz.pavel.taskmanagement.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public List<TaskResponseDTO> getAllTasks() {
        log.info("Fetching all tasks");
        return taskRepository.findAll().stream()
                .map(TaskResponseDTO::new)
                .collect(Collectors.toList());
    }

    public TaskResponseDTO getTaskById(Long id) {
        log.info("Fetching task with id: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
        return new TaskResponseDTO(task);
    }

    public List<TaskResponseDTO> getTasksByProject(Long projectId) {
        log.info("Fetching tasks for project id: {}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        return taskRepository.findByProject(project).stream()
                .map(TaskResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<TaskResponseDTO> getTasksByAssignee(Long assigneeId) {
        log.info("Fetching tasks for assignee id: {}", assigneeId);
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("User", assigneeId));

        return taskRepository.findByAssignee(assignee).stream()
                .map(TaskResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<TaskResponseDTO> getTasksByStatus(TaskStatus status) {
        log.info("Fetching tasks with status: {}", status);
        return taskRepository.findByStatus(status).stream()
                .map(TaskResponseDTO::new)
                .collect(Collectors.toList());
    }

    public TaskResponseDTO createTask(TaskCreateDTO createDTO) {
        log.info("Creating new task: {}", createDTO.getTitle());

        Project project = projectRepository.findById(createDTO.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", createDTO.getProjectId()));

        Task task = new Task();
        task.setTitle(createDTO.getTitle());
        task.setDescription(createDTO.getDescription());
        task.setStatus(createDTO.getStatus());
        task.setPriority(createDTO.getPriority());
        task.setDueDate(createDTO.getDueDate());
        task.setProject(project);

        if (createDTO.getAssigneeId() != null) {
            User assignee = userRepository.findById(createDTO.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", createDTO.getAssigneeId()));
            task.setAssignee(assignee);
        }

        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully with id: {}", savedTask.getId());

        return new TaskResponseDTO(savedTask);
    }

    public TaskResponseDTO updateTask(Long id, TaskUpdateDTO updateDTO) {
        log.info("Updating task with id: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));

        if (updateDTO.getTitle() != null) {
            task.setTitle(updateDTO.getTitle());
        }

        if (updateDTO.getDescription() != null) {
            task.setDescription(updateDTO.getDescription());
        }

        if (updateDTO.getStatus() != null) {
            task.setStatus(updateDTO.getStatus());
        }

        if (updateDTO.getPriority() != null) {
            task.setPriority(updateDTO.getPriority());
        }

        if (updateDTO.getDueDate() != null) {
            task.setDueDate(updateDTO.getDueDate());
        }

        if (updateDTO.getAssigneeId() != null) {
            User assignee = userRepository.findById(updateDTO.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", updateDTO.getAssigneeId()));
            task.setAssignee(assignee);
        }

        Task updatedTask = taskRepository.save(task);
        log.info("Task updated successfully with id: {}", updatedTask.getId());

        return new TaskResponseDTO(updatedTask);
    }

    public void deleteTask(Long id) {
        log.info("Deleting task with id: {}", id);

        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task", id);
        }

        taskRepository.deleteById(id);
        log.info("Task deleted successfully with id: {}", id);
    }
}