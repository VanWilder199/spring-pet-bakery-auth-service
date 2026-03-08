package buloshnaya.authService.dto;

import buloshnaya.authService.model.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    @JsonIgnore
    private String refreshToken;
    private Role role;
}
