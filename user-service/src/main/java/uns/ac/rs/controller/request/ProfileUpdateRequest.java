package uns.ac.rs.controller.request;

import jakarta.validation.constraints.NotBlank;
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
    @NotBlank
    private String username;
    @Pattern(regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$", message = "Invalid email address")
    private String email;
    @Size(max = 200, message = "First name cannot have more than 200 characters")
    @NotBlank
    private String firstName;
    @Size(max = 200, message = "Last name cannot have more than 200 characters")
    @NotBlank
    private String lastName;
    @Size(max = 200, message = "City cannot have more than 1000 characters")
    @NotBlank
    private String city;
}
