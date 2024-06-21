package uns.ac.rs.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import uns.ac.rs.controller.events.NotificationType;
import uns.ac.rs.entity.NotificationSettings;
import uns.ac.rs.entity.User;

import java.util.List;

@ApplicationScoped
public class NotificationSettingsRepository implements PanacheRepository<NotificationSettings> {

    public NotificationSettings findByUsername(String username) {
        List<NotificationSettings> results = getEntityManager().createQuery(
                        "SELECT ns FROM NotificationSettings ns JOIN ns.user u WHERE u.username = :username", NotificationSettings.class)
                .setParameter("username", username)
                .getResultList();

        if (results.isEmpty()) {
            throw new NoResultException("No NotificationSettings found for username: " + username);
        }

        return results.get(0);
    }


    @Transactional
    public boolean isNotificationEnabled(String username, NotificationType notificationType) {
        NotificationSettings settings = findByUsername(username);
        return switch (notificationType) {
            case RESERVATION_REQUEST_CREATED -> settings.isReservationRequestCreated();
            case RESERVATION_DECLINED -> settings.isReservationDeclined();
            case PERSONAL_REVIEW -> settings.isPersonalReview();
            case ACCOMMODATION_REVIEW -> settings.isAccommodationReview();
            case RESERVATION_REQUEST_RESOLVED -> settings.isReservationRequestResolved();
            default -> throw new IllegalArgumentException("Unknown NotificationType: " + notificationType);
        };
    }
}
