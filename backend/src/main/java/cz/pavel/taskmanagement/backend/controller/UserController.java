package cz.pavel.taskmanagement.backend.controller;

import cz.pavel.taskmanagement.backend.dto.user.UserCreateDTO;
import cz.pavel.taskmanagement.backend.dto.user.UserResponseDTO;
import cz.pavel.taskmanagement.backend.dto.user.UserUpdateDTO;
import cz.pavel.taskmanagement.backend.security.SecurityUtils;
import cz.pavel.taskmanagement.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User controller", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieve a list of all users in the system (admin only)")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        log.info("GET /api/users - Fetching all users");
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by their ID (own profile or admin)")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long id) {
        if (!SecurityUtils.isAdminOrCurrentUser(id)) {
            throw new AccessDeniedException("You can only view your own profile");
        }
        log.info("GET /api/users/{} - Fetching user by id", id);
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new user", description = "Register a new user in the system (admin only)")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        log.info("POST /api/users - Creating new user: {}", userCreateDTO.getUsername());
        UserResponseDTO user = userService.createUser(userCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update an existing user's information (own profile or admin)")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO userUpdateDTO
            ) {
        if (!SecurityUtils.isAdminOrCurrentUser(id)) {
            throw new AccessDeniedException("You can only update your own profile");
        }
        // Non-admin users cannot change their own role
        if (!SecurityUtils.isAdmin() && userUpdateDTO.getRole() != null) {
            throw new AccessDeniedException("Only admins can change user roles");
        }
        log.info("PUT /api/users/{} - Updating user: ", id);
        UserResponseDTO user = userService.updateUser(id, userUpdateDTO);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Remove a user from the system (admin only)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/user/{} - Deleting user: ", id);
        userService.deleteUser((id));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/by-email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user by email", description = "Remove a user from the system by email (admin only)")
    public ResponseEntity<Void> deleteUserByEmail(@PathVariable String email) {
        log.info("DELETE /api/users/by-email/{} - Deleting user by email: ", email);
        userService.deleteUserByEmail(email);
        return ResponseEntity.noContent().build();
    }
}
