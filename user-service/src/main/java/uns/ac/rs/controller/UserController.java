package uns.ac.rs.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.ResponseStatus;
import uns.ac.rs.controller.request.RegistrationRequest;
import uns.ac.rs.entity.Role;
import uns.ac.rs.entity.User;
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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @PermitAll
    @ResponseStatus(201)
    public void register(@Valid RegistrationRequest registrationRequest) {
        var user = new User(
            registrationRequest.getUsername(),
            registrationRequest.getEmail(),
            Role.valueOf(registrationRequest.getRole()),
            registrationRequest.getFirstName(),
            registrationRequest.getLastName(),
            registrationRequest.getCity()
        );
        userService.register(user, registrationRequest.getPassword());
    }
}
