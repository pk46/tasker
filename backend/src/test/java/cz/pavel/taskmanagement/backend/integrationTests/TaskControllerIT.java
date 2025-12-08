package cz.pavel.taskmanagement.backend.integrationTests;

import cz.pavel.taskmanagement.backend.dto.task.TaskCreateDTO;
import cz.pavel.taskmanagement.backend.dto.task.TaskUpdateDTO;
import cz.pavel.taskmanagement.backend.entity.Project;
import cz.pavel.taskmanagement.backend.entity.Role;
import cz.pavel.taskmanagement.backend.entity.Task;
import cz.pavel.taskmanagement.backend.entity.User;
import cz.pavel.taskmanagement.backend.repository.ProjectRepository;
import cz.pavel.taskmanagement.backend.repository.TaskRepository;
import cz.pavel.taskmanagement.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class TaskControllerIT extends Testutils {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private String adminToken;
    private Project testProject;

    @BeforeEach
    void setup() throws Exception {
        User admin = User.builder()
                .username("test_admin")
                .email("test_admin@pavel.cz")
                .password(passwordEncoder.encode("password_test"))
                .firstName("Test")
                .lastName("Admin")
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);

        testProject = new Project();
        testProject.setName("Test Project");
        testProject.setDescription("Project for testing");
        testProject.setOwner(admin);
        projectRepository.save(testProject);

        adminToken = loginAndGetToken("test_admin", "password_test");
    }

    @Test
    void createTask_WithValidData_ShouldReturn201AndSaveToDatabase() throws Exception {
        TaskCreateDTO task = new TaskCreateDTO();
        task.setTitle("Integration Test Task");
        task.setDescription("Testing full stack");
        task.setProjectId(testProject.getId());

        String jsonRequest = objectMapper.writeValueAsString(task);

        assertEquals(0, taskRepository.count());

        mockMvc.perform(
                        post("/api/tasks")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonRequest)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Integration Test Task"))
                .andExpect(jsonPath("$.projectId").value(testProject.getId()))
                .andReturn();

        assertEquals(1, taskRepository.count());

        Task savedTask = taskRepository.findAll().getFirst();
        assertEquals("Integration Test Task", savedTask.getTitle());
        assertEquals(testProject.getId(), savedTask.getProject().getId());
    }

    @Test
    void createTask_WithoutAuthentication_ShouldReturn401() throws Exception {
        TaskCreateDTO task = new TaskCreateDTO();
        task.setTitle("Task");
        task.setProjectId(testProject.getId());

        String jsonRequest = objectMapper.writeValueAsString(task);

        mockMvc.perform(
                        post("/api/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonRequest)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        assertEquals(0, taskRepository.count());
    }

    @Test
    void getAllTasks_ShouldReturnAllTasksFromDatabase() throws Exception {
        for (int i = 1; i <= 3; i++) {
            Task task = new Task();
            task.setTitle("Task " + i);
            task.setDescription("Description " + i);
            task.setProject(testProject);
            taskRepository.save(task);
        }

        assertEquals(3, taskRepository.count());

        mockMvc.perform(
                get("/api/tasks")
                        .header("Authorization", "Bearer " + adminToken)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[1].title").value("Task 2"))
                .andExpect(jsonPath("$[2].title").value("Task 3"))
                .andExpect(jsonPath("$[0].description").value("Description 1"))
                .andExpect(jsonPath("$[1].description").value("Description 2"))
                .andExpect(jsonPath("$[2].description").value("Description 3"))
                .andExpect(jsonPath("$[0].projectId").value(testProject.getId()))
                .andExpect(jsonPath("$[1].projectId").value(testProject.getId()))
                .andExpect(jsonPath("$[2].projectId").value(testProject.getId()));
    }

    @Test
    void deleteTask_ShouldRemoveFromDatabase() throws Exception {
        Task task = new Task();
        task.setTitle("Task to delete");
        task.setProject(testProject);
        Task savedTask = taskRepository.save(task);
        Long taskId = savedTask.getId();

        assertEquals(1, taskRepository.count());

        mockMvc.perform(
                        delete("/api/tasks/{taskId}", taskId)
                                .header("Authorization", "Bearer " + adminToken)
                )
                .andExpect(status().isNoContent());
        assertEquals(0, taskRepository.count());
        assertFalse(taskRepository.existsById(taskId));
    }

    @Test
    void editTask_ShouldChangeTask() throws Exception {
        Task task = new Task();
        task.setTitle("Task to edit");
        task.setDescription("New task description");
        task.setProject(testProject);
        taskRepository.save(task);

        assertEquals(1, taskRepository.count());

        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setTitle("Edited task");
        updateDTO.setDescription("Old task description");

        String jsonRequest = objectMapper.writeValueAsString(updateDTO);

        mockMvc.perform(
                put("/api/tasks/{task}", task.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Edited task"))
                .andExpect(jsonPath("$.description").value("Old task description"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.assigneeId").doesNotExist())
                .andExpect(jsonPath("$.projectId").value(testProject.getId()))
                .andExpect(jsonPath("$.id").value(task.getId()));

    }
}
