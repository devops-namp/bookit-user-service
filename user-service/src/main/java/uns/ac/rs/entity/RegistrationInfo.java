package uns.ac.rs.entity;

import jakarta.persistence.*;
import lombok.*;
import uns.ac.rs.controller.request.RegistrationRequest;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "registration_info")
public class RegistrationInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(unique=true)
    private String username;
    private String password;
    @Column(unique=true)
    private String email;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String firstName;
    private String lastName;
    private String city;
    private String code;
    private LocalDateTime timestamp;

    public RegistrationInfo(RegistrationRequest registrationRequest) {
        this.username = registrationRequest.getUsername();
        this.email = registrationRequest.getEmail();
        this.role = Role.valueOf(registrationRequest.getRole());
        this.firstName = registrationRequest.getFirstName();
        this.lastName = registrationRequest.getLastName();
        this.city = registrationRequest.getCity();
        this.timestamp = LocalDateTime.now();
    }
}
