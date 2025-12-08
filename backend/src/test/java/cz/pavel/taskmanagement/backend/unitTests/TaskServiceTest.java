package cz.pavel.taskmanagement.backend.unitTests;

import cz.pavel.taskmanagement.backend.dto.task.TaskCreateDTO;
import cz.pavel.taskmanagement.backend.dto.task.TaskResponseDTO;
import cz.pavel.taskmanagement.backend.entity.Priority;
import cz.pavel.taskmanagement.backend.entity.Project;
import cz.pavel.taskmanagement.backend.entity.Task;
import cz.pavel.taskmanagement.backend.entity.TaskStatus;
import cz.pavel.taskmanagement.backend.exception.ResourceNotFoundException;
import cz.pavel.taskmanagement.backend.repository.ProjectRepository;
import cz.pavel.taskmanagement.backend.repository.TaskRepository;
import cz.pavel.taskmanagement.backend.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private TaskService taskService;

    private Project testProject;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setDescription("Project for testing");

        testTask = new Task();
        testTask.setId(100L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Task description");
        testTask.setStatus(TaskStatus.TODO);
        testTask.setPriority(Priority.MEDIUM);
        testTask.setProject(testProject);
    }

    @Test
    void createTask_WithValidData_ShouldReturnCreatedTask() {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setTitle("New Task");
        dto.setDescription("Task description");
        dto.setProjectId(1L);
        dto.setStatus(TaskStatus.TODO);
        dto.setPriority(Priority.HIGH);

        when(projectRepository.findById(1L))
                .thenReturn(Optional.of(testProject));

        when(taskRepository.save(any(Task.class)))
                .thenReturn(testTask);

        TaskResponseDTO result = taskService.createTask(dto);

        assertNotNull(result, "Result should not be null");
        assertEquals(100L, result.getId(), "Task ID should be 100");
        assertEquals("Test Task", result.getTitle(), "Task title should match");

        verify(projectRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createTask_WithNonExistentProject_ShouldThrowException() {
        TaskCreateDTO task = new TaskCreateDTO();
        task.setTitle("Task");
        task.setProjectId(1L);

        when(projectRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> taskService.createTask(task),
                        "ResourceNotFoundException should be thrown");

        assertEquals("Project with id 1 not found", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }
}
