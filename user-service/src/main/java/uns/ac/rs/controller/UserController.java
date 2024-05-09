package uns.ac.rs.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import uns.ac.rs.service.UserService;

@Path("/users")
public class UserController {

    @Inject
    UserService userService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return "users";
    }
}
