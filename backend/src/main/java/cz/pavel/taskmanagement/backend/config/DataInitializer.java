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
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (Arrays.asList(args).contains("--init-data")) {
            log.info("Initializing database with sample data...");
            createUser();
        }
    }

    private void createUser() {
        User user = new User();
        user.setUsername("admin");
        user.setFirstName("Admin");
        user.setLastName("Admin");
        user.setEmail("");
        user.setRole(Role.ADMIN);

        String hashedPassword = passwordEncoder.encode("admin");
        user.setPassword(hashedPassword);

        userRepository.save(user);

        log.info("Created admin with username: admin and password: admin");
    }
}