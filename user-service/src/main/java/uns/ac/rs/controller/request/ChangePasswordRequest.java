package uns.ac.rs.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {
    @NotBlank(message = "Username cannot be empty")
    @NotNull(message = "Username cannot be null")
    private String username;
    @NotBlank(message = "Current password cannot be empty")
    @NotNull(message = "Current password cannot be null")
    private String currentPassword;
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$", message = "Password must be at least 6 characters long, contain at least one letter and one number")
    @NotNull(message = "Password cannot be null")
    private String newPassword;
}
