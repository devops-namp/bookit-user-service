package uns.ac.rs.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uns.ac.rs.entity.User;

@Getter
@AllArgsConstructor
public class UserDTO {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String city;
    private String role;

    public UserDTO(User user) {
        this(
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getCity(),
            user.getRole().name()
        );
    }
}
