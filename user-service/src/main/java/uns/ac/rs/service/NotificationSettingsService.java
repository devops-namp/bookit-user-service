package uns.ac.rs.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import uns.ac.rs.controller.events.NotificationEvent;
import uns.ac.rs.controller.events.NotificationType;
import uns.ac.rs.repository.NotificationSettingsRepository;
import uns.ac.rs.repository.UserRepository;

@ApplicationScoped
public class NotificationSettingsService {

    @Inject
    @Channel("notification-check-queue")
    Emitter<NotificationEvent> notificationEmitter;

    @Inject
    NotificationSettingsRepository notificationSettingsRepository;

    @Inject
    UserRepository userRepository;

    public void propagate(NotificationEvent event) {
        if (notificationSettingsRepository.isNotificationEnabled(event.getRecipientId(), event.getNotificationType())) {
            notificationEmitter.send(event);
        }
    }

}
