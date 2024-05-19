package uns.ac.rs.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmRegistrationRequest {
    @NotBlank(message = "Email cannot be empty")
    @NotNull(message = "Email cannot be null")
    private String email;
    @NotBlank(message = "Code cannot be empty")
    @NotNull(message = "Code cannot be null")
    private String code;
}
