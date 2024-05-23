package uns.ac.rs.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {
    @NotBlank(message="Username cannot be empty")
    @NotNull(message = "Username cannot be null")
    @Size(max = 200, message = "Username cannot have more than 200 characters")
    private String username;
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$", message = "Password must be at least 6 characters long, contain at least one letter and one number")
    @NotNull(message = "Password cannot be null")
    private String password;
    @NotBlank(message = "Email cannot be empty")
    @NotNull(message = "Email cannot be null")
    @Pattern(regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$", message = "Invalid email address")
    private String email;
    @NotBlank(message = "First name cannot be empty")
    @NotNull(message = "First name cannot be null")
    @Size(max = 200, message = "First name cannot have more than 200 characters")
    private String firstName;
    @NotBlank(message = "Last name cannot be empty")
    @NotNull(message = "Last name cannot be null")
    @Size(max = 200, message = "Last name cannot have more than 200 characters")
    private String lastName;
    @NotBlank(message = "City cannot be empty")
    @NotNull(message = "City cannot be null")
    @Size(max = 200, message = "City cannot have more than 1000 characters")
    private String city;
    @Pattern(regexp = "GUEST|HOST", message = "Role can be GUEST or HOST")
    @NotNull(message = "Role cannot be null")
    private String role;
}
