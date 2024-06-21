package uns.ac.rs.controller.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    private String text;
    private String recipientId;
    private NotificationType notificationType;
    //private LocalDateTime time;


}
