package uns.ac.rs.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileUpdateRequest {
    @Size(max = 200, message = "Username cannot have more than 200 characters")
    @NotBlank(message = "Username cannot be blank")
    private String username;
    @NotNull(message = "Email cannot be null")
    @Pattern(regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$", message = "Invalid email address")
    private String email;
    @Size(max = 200, message = "First name cannot have more than 200 characters")
    @NotBlank(message = "First name cannot be blank")
    private String firstName;
    @Size(max = 200, message = "Last name cannot have more than 200 characters")
    @NotBlank(message = "Last name cannot be blank")
    private String lastName;
    @Size(max = 200, message = "City cannot have more than 1000 characters")
    @NotBlank(message = "City cannot be blank")
    private String city;
}
