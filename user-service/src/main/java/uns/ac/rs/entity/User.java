package uns.ac.rs.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bookit_users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(unique=true)
    private String username;
    @Setter
    private String password;
    @Column(unique=true)
    private String email;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String firstName;
    private String lastName;
    private String city;

    public User(TempUser tempUser) {
        this.username = tempUser.getUsername();
        this.password = tempUser.getPassword();
        this.email = tempUser.getEmail();
        this.role = tempUser.getRole();
        this.firstName = tempUser.getFirstName();
        this.lastName = tempUser.getLastName();
        this.city = tempUser.getCity();
    }
}
