package cz.pavel.taskmanagement.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static AuthenticatedUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser) {
            return (AuthenticatedUser) auth.getPrincipal();
        }
        throw new IllegalStateException("No authenticated user found");
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(getCurrentUser().getRole());
    }

    public static boolean isCurrentUser(Long userId) {
        return getCurrentUser().getUserId().equals(userId);
    }

    public static boolean isAdminOrCurrentUser(Long userId) {
        return isAdmin() || isCurrentUser(userId);
    }
}
