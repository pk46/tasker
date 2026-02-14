package cz.pavel.taskmanagement.backend.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuthenticatedUser {
    private final String username;
    private final Long userId;
    private final String role;

    @Override
    public String toString() {
        return username;
    }
}
