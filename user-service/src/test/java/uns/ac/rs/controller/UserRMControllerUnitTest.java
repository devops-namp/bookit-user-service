package uns.ac.rs.controller;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uns.ac.rs.service.UserService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class UserRMControllerUnitTest {
    @Mock
    UserService userService;

    @InjectMocks
    UserController userController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSetAutoapproveFalse() {
        String username = "testUser";

        userController.setAutoapproveFalse(username);

        verify(userService, times(1)).changeAutoapprove(username, false);
    }

    @Test
    public void testSetAutoapproveTrue() {
        String username = "testUser";

        userController.setAutoapproveTrue(username);

        verify(userService, times(1)).changeAutoapprove(username, true);
    }

    @Test
    public void testGetAutoapproveStatus() {
        String username = "testUser";
        boolean autoapproveStatus = true;
        when(userService.getAutoapprove(username)).thenReturn(autoapproveStatus);

        Response response = userController.getAutoapproveStatus(username);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(autoapproveStatus, response.getEntity());
        verify(userService, times(1)).getAutoapprove(username);
    }

    @Test
    public void testGetRejectCount() {
        List<String> usernames = List.of("user1", "user2");
        Map<String, Integer> rejectCounts = Map.of("user1", 3, "user2", 5);
        when(userService.getRejectCounts(usernames)).thenReturn(rejectCounts);

        Response response = userController.getRejectCount(usernames);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(rejectCounts, response.getEntity());
        verify(userService, times(1)).getRejectCounts(usernames);
    }
}
