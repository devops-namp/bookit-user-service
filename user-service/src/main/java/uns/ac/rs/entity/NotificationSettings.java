package uns.ac.rs.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notification-settings")
public class NotificationSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private boolean reservationRequestCreated;
    private boolean reservationDeclined;
    private boolean personalReview;
    private boolean accommodationReview;
    private boolean reservationRequestResolved;

    @OneToOne(mappedBy = "notificationSettings"/*, cascade = CascadeType.ALL*/)
    @JsonIgnore
    private User user;

    public NotificationSettings(User user) {
        this.user = user;
        this.reservationRequestCreated = true;
        this.reservationDeclined = true;
        this.personalReview = true;
        this.accommodationReview = true;
        this.reservationRequestResolved = true;
    }

    public NotificationSettings(boolean b1, boolean b2, boolean b3, boolean b4, boolean b5 ) {
        this.reservationRequestCreated = true;
        this.reservationDeclined = true;
        this.personalReview = true;
        this.accommodationReview = true;
        this.reservationRequestResolved = true;
    }
}

