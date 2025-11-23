package cz.pavel.taskmanagement.backend.dto.auth;

import cz.pavel.taskmanagement.backend.dto.user.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";
    private UserResponseDTO user;

    public LoginResponse(String token, String refreshToken, UserResponseDTO user) {
        this.accessToken = token;
        this.refreshToken = refreshToken;
        this.user = user;
    }
}
