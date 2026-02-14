package cz.pavel.taskmanagement.backend.controller;

import cz.pavel.taskmanagement.backend.dto.auth.LoginRequest;
import cz.pavel.taskmanagement.backend.dto.auth.LoginResponse;
import cz.pavel.taskmanagement.backend.dto.auth.RefreshTokenRequest;
import cz.pavel.taskmanagement.backend.security.LoginRateLimiter;
import cz.pavel.taskmanagement.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthService authService;
    private final LoginRateLimiter rateLimiter;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        String rateLimitKey = clientIp + ":" + request.getUsername();

        if (rateLimiter.isBlocked(rateLimitKey)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        try {
            LoginResponse loginResponse = authService.login(request);
            rateLimiter.recordSuccessfulLogin(rateLimitKey);
            return ResponseEntity.ok(loginResponse);
        } catch (BadCredentialsException e) {
            rateLimiter.recordFailedAttempt(rateLimitKey);
            throw e;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
