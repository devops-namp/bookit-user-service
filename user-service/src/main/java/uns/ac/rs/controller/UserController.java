package uns.ac.rs.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.ResponseStatus;
import uns.ac.rs.controller.request.ConfirmRegistrationRequest;
import uns.ac.rs.controller.request.RegistrationRequest;
import uns.ac.rs.entity.RegistrationInfo;
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
    @PermitAll
    @ResponseStatus(201)
    public void register(@Valid RegistrationRequest registrationRequest) {
        var registrationInfo = new RegistrationInfo(registrationRequest);
        userService.saveRegistrationInfo(registrationInfo, registrationRequest.getPassword());
    }

    @POST
    @Path("/confirm")
    @PermitAll
    @ResponseStatus(201)
    public void confirmRegistration(@Valid ConfirmRegistrationRequest request) {
        userService.confirmRegistration(request.getEmail(), request.getCode());
    }
}
