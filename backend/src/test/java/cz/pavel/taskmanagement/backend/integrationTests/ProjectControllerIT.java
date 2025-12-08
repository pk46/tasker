package cz.pavel.taskmanagement.backend.integrationTests;

import cz.pavel.taskmanagement.backend.dto.project.ProjectCreateDTO;
import cz.pavel.taskmanagement.backend.dto.project.ProjectUpdateDTO;
import cz.pavel.taskmanagement.backend.entity.Project;
import cz.pavel.taskmanagement.backend.entity.Role;
import cz.pavel.taskmanagement.backend.entity.User;
import cz.pavel.taskmanagement.backend.repository.ProjectRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class ProjectControllerIT extends Testutils {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User admin;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        admin = User.builder()
                .username("test_admin")
                .email("test_admin@pavel.cz")
                .password(passwordEncoder.encode("password_test"))
                .firstName("Test")
                .lastName("Admin")
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);

        adminToken = loginAndGetToken("test_admin", "password_test");
    }

    @Test
    void createProject_WithValidData_ShouldReturn201AndSaveToDatabase() throws Exception {
        ProjectCreateDTO project = new ProjectCreateDTO();
        project.setName("Test Project");
        project.setDescription("Project for testing");

        String jsonRequest = objectMapper.writeValueAsString(project);

        mockMvc.perform(
                post("/api/projects")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("ownerId", admin.getId().toString())
                        .content(jsonRequest)
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Project"))
                .andExpect(jsonPath("$.description").value("Project for testing"))
                .andExpect(jsonPath("$.owner.id").value(admin.getId().toString()))
                .andExpect(jsonPath("$.owner.username").value("test_admin"))
                .andExpect(jsonPath("$.owner.role").value("ADMIN"));

            assertEquals(1, projectRepository.count());

            Project savedProject = projectRepository.findAll().getFirst();
            assertEquals("Test Project", savedProject.getName());
            assertEquals("Project for testing", savedProject.getDescription());
    }

    @Test
    void editProject_ShouldChangeProjectInDatabase() throws Exception {
        Project project = new Project();
        project.setName("Test Project");
        project.setDescription("Project for testing");
        project.setOwner(admin);
        Project savedProject = projectRepository.save(project);
        Long projectId = savedProject.getId();

        ProjectUpdateDTO updateDTO = new ProjectUpdateDTO();
        updateDTO.setName("Edited Project");
        updateDTO.setDescription("Edited description");

        assertEquals(1, projectRepository.count());

        String jsonRequest = objectMapper.writeValueAsString(updateDTO);

        mockMvc.perform(
                put("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Edited Project"))
                .andExpect(jsonPath("$.description").value("Edited description"));
        assertEquals(1, projectRepository.count());

        Project updatedProject = projectRepository.findAll().getFirst();
        assertEquals("Edited Project", updatedProject.getName());
        assertEquals("Edited description", updatedProject.getDescription());
    }

    @Test
    void deleteProject_ShouldRemoveFromDatabase() throws Exception {
        Project project = new Project();
        project.setName("Test Project to delete");
        project.setDescription("Project for deletion testing");
        project.setOwner(admin);
        Project savedProject = projectRepository.save(project);
        Long projectId = savedProject.getId();

        assertEquals(1, projectRepository.count());

        mockMvc.perform(
                delete("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + adminToken)
        )
                .andExpect(status().isNoContent());
        assertEquals(0, projectRepository.count());
        assertFalse(projectRepository.existsById(projectId));
    }

    @Test
    void deleteProject_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteProject_UserWithoutPermission_ShouldReturn403() throws Exception {
        User user = User.builder()
            .username("test_user")
            .email("test_user@pavel.cz")
            .password(passwordEncoder.encode("password_test"))
            .firstName("Test")
            .lastName("User")
            .role(Role.USER)
            .build();

        userRepository.save(user);

        String userToken = loginAndGetToken("test_user", "password_test");

        Project project = new Project();
        project.setName("Test Project to delete");
        project.setDescription("Project for deletion testing");
        project.setOwner(admin);
        Project savedProject = projectRepository.save(project);
        Long projectId = savedProject.getId();

        mockMvc.perform(
                delete("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllProjects_ShouldReturnAllProjectsFromDatabase() throws Exception {
        for (int i = 1; i <= 3; i++) {
            Project project = new Project();
            project.setName("Project " + i);
            project.setDescription("Project for testing " + i);
            project.setOwner(admin);
            projectRepository.save(project);
        }

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

    }
}
