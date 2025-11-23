package cz.pavel.taskmanagement.backend.service;

import cz.pavel.taskmanagement.backend.dto.auth.LoginRequest;
import cz.pavel.taskmanagement.backend.dto.auth.LoginResponse;
import cz.pavel.taskmanagement.backend.dto.auth.RefreshTokenRequest;
import cz.pavel.taskmanagement.backend.dto.user.UserResponseDTO;
import cz.pavel.taskmanagement.backend.entity.User;
import cz.pavel.taskmanagement.backend.repository.UserRepository;
import cz.pavel.taskmanagement.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for username: {}", request.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }

        String accessToken = jwtUtil.generateAccessToken(
                user.getUsername(),
                user.getId(),
                user.getRole().name()
        );

        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        log.info("User {} logged in successfully", user.getUsername());
        return new LoginResponse(accessToken, refreshToken, new UserResponseDTO(user));
    }

    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        String userName = jwtUtil.getUsernameFromToken(refreshToken);

        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        String newAccessToken = jwtUtil.generateAccessToken(
                user.getUsername(),
                user.getId(),
                user.getRole().name()
        );

        String newRefreshTOken = jwtUtil.generateRefreshToken(
                user.getUsername()
        );

        log.info("Tokens refreshed for user", userName);
        return new LoginResponse(newAccessToken, newRefreshTOken, new UserResponseDTO(user));
    }
}
