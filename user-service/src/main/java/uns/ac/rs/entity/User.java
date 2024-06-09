package uns.ac.rs.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bookit-users")
public class User {
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

    public User(RegistrationInfo registrationInfo) {
        this.username = registrationInfo.getUsername();
        this.password = registrationInfo.getPassword();
        this.email = registrationInfo.getEmail();
        this.role = registrationInfo.getRole();
        this.firstName = registrationInfo.getFirstName();
        this.lastName = registrationInfo.getLastName();
        this.city = registrationInfo.getCity();
    }
}
