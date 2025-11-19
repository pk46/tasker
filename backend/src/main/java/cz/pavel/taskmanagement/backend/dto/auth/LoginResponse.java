package cz.pavel.taskmanagement.backend.dto.auth;

import cz.pavel.taskmanagement.backend.dto.user.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String type = "Bearer";
    private UserResponseDTO user;

    public LoginResponse(String token, UserResponseDTO user) {
        this.token = token;
        this.user = user;
    }
}
