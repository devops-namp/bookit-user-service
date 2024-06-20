package uns.ac.rs.controller.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AutoApproveEvent {
    private String username;
    private Long accommodationId;
    private boolean autoapprove;
    private AutoApproveEventType type;


    public AutoApproveEvent(String u, Long id, AutoApproveEventType t) {
        this.username = u;
        this.accommodationId = id;
        this.type = t;
    }

    public enum AutoApproveEventType {
        GET_BY_USER, CHANGE, INCREMENT
    }
}
