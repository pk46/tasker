package cz.pavel.taskmanagement.backend.controller;

import cz.pavel.taskmanagement.backend.entity.*;
import cz.pavel.taskmanagement.backend.repository.ProjectRepository;
import cz.pavel.taskmanagement.backend.repository.TaskRepository;
import cz.pavel.taskmanagement.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    @GetMapping("/seed")
    public Map<String, String> seedDatabase() {
        User user = new User();
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setPassword("password123");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.USER);
        user = userRepository.save(user);

        Project project = new Project();
        project.setName("My First Project");
        project.setDescription("This is a test project");
        project.setOwner(user);
        project = projectRepository.save(project);

        Task task = new Task();
        task.setTitle("Implement login feature");
        task.setDescription("Create JWT authentication");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(Priority.HIGH);
        task.setDueDate(LocalDate.now().plusDays(7));
        task.setProject(project);
        task.setAssignee(user);
        taskRepository.save(task);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Database seeded successfully!");
        response.put("user", user.getUsername());
        response.put("project", project.getName());
        response.put("task", task.getTitle());
        return response;
    }

    @GetMapping("/data")
    public Map<String, Object> getAllData() {
        Map<String, Object> response = new HashMap<>();
        response.put("users", userRepository.findAll());
        response.put("projects", projectRepository.findAll());
        response.put("tasks", taskRepository.findAll());
        return response;
    }
}
