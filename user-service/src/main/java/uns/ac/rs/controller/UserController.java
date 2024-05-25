package uns.ac.rs.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.ResponseStatus;
import uns.ac.rs.controller.dto.UserDTO;
import uns.ac.rs.controller.request.ConfirmRegistrationRequest;
import uns.ac.rs.controller.request.ProfileUpdateRequest;
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

    @PUT
    @Path("/{username}")
    @RolesAllowed({ "GUEST", "HOST" })
    @ResponseStatus(200)
    public UserDTO updateProfile(@PathParam("username") String currentUsername, @Valid ProfileUpdateRequest request) {
        return new UserDTO(
            userService.updateProfile(
                currentUsername,
                request.getUsername(),
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getCity()
            )
        );
    }
}
