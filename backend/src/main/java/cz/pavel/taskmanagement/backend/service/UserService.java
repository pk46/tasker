package cz.pavel.taskmanagement.backend.service;

import cz.pavel.taskmanagement.backend.dto.user.UserCreateDTO;
import cz.pavel.taskmanagement.backend.dto.user.UserResponseDTO;
import cz.pavel.taskmanagement.backend.dto.user.UserUpdateDTO;
import cz.pavel.taskmanagement.backend.entity.Role;
import cz.pavel.taskmanagement.backend.entity.User;
import cz.pavel.taskmanagement.backend.exception.DuplicateResourceException;
import cz.pavel.taskmanagement.backend.exception.ResourceNotFoundException;
import cz.pavel.taskmanagement.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponseDTO> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll()
                .stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());
    }

    public UserResponseDTO getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return new UserResponseDTO(user);
    }

    public UserResponseDTO createUser(UserCreateDTO createDTO) {
        log.info("Creating new user with username: {}", createDTO.getUsername());

        if (userRepository.existsByUsername(createDTO.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + createDTO.getUsername());
        }

        if (userRepository.existsByEmail(createDTO.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + createDTO.getEmail());
        }

        User user = new User();
        user.setUsername(createDTO.getUsername());
        user.setEmail(createDTO.getEmail());
        user.setPassword(createDTO.getPassword());
        user.setFirstName(createDTO.getFirstName());
        user.setLastName(createDTO.getLastName());
        user.setRole(createDTO.getRole());

        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {}", savedUser.getId());

        return new UserResponseDTO(savedUser);
    }

    public UserResponseDTO updateUser(Long id, UserUpdateDTO updateDTO) {
        log.info("Updating user with id: {}", id);

        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (updateDTO.getEmail() != null) {
            userRepository.findByEmail((updateDTO.getEmail()))
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(id)) {
                            throw new DuplicateResourceException("Email already exists: " + updateDTO.getEmail());
                        }
                    });
            user.setEmail(updateDTO.getEmail());
        }

        if (updateDTO.getPassword() != null) {
            user.setPassword(updateDTO.getPassword());
        }

        if (updateDTO.getFirstName() != null) {
            user.setFirstName(updateDTO.getFirstName());
        }

        if (updateDTO.getLastName() != null) {
            user.setLastName(updateDTO.getLastName());
        }

        if (updateDTO.getRole() != null) {
            user.setRole(updateDTO.getRole());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with id: {}", updatedUser.getId());

        return new UserResponseDTO(updatedUser);
    }

    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully with id: {}", id);
    }
}
