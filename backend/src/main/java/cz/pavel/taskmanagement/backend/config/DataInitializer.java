package cz.pavel.taskmanagement.backend.config;

import cz.pavel.taskmanagement.backend.entity.Role;
import cz.pavel.taskmanagement.backend.entity.User;
import cz.pavel.taskmanagement.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default-password:#{null}}")
    private String adminDefaultPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (Arrays.asList(args).contains("--init-data")) {
            log.info("Initializing database with sample data...");
            createUser();
        }
    }

    private void createUser() {
        if (userRepository.existsByUsername("admin")) {
            log.info("Admin user already exists, skipping creation");
            return;
        }

        String password = adminDefaultPassword;
        if (password == null || password.isBlank()) {
            password = java.util.UUID.randomUUID().toString();
            log.warn("No admin.default-password configured. Generated random password - check application startup logs.");
            log.info("Generated admin password: {}", password);
        }

        User user = new User();
        user.setUsername("admin");
        user.setFirstName("Admin");
        user.setLastName("Admin");
        user.setEmail("admin@localhost");
        user.setRole(Role.ADMIN);

        String hashedPassword = passwordEncoder.encode(password);
        user.setPassword(hashedPassword);

        userRepository.save(user);

        log.info("Created admin user with username: admin");
    }
}