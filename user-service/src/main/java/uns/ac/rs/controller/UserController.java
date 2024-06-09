package uns.ac.rs.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import io.vertx.core.json.JsonObject;


import org.jboss.resteasy.reactive.ResponseStatus;

import uns.ac.rs.controller.dto.UpdatedUserDTO;
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

    @Inject
    @Channel("filter-response-queue")
    Emitter<Book> bookEmitter;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return "users";
    }

    @Incoming("filter-request-queue")
    public void consume(String dobavi) {
        Book book = new Book("Cvece", "Al");
        System.out.println("Dobio sam zahtev da treba da ti dobavim knjigu");
        bookEmitter.send(book);

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

    @GET
    @Path("/{username}")
    @RolesAllowed({ "GUEST", "HOST" })
    @ResponseStatus(200)
    public UserDTO get(@PathParam("username") String username) {
        return new UserDTO(userService.get(username));
    }

    @PUT
    @Path("/{username}")
    @RolesAllowed({ "GUEST", "HOST" })
    @ResponseStatus(200)
    public UpdatedUserDTO updateProfile(@PathParam("username") String currentUsername, @Valid ProfileUpdateRequest request) {
        var updatedUserInfo = userService.updateProfile(
            currentUsername,
            request.getUsername(),
            request.getEmail(),
            request.getFirstName(),
            request.getLastName(),
            request.getCity()
        );
        return new UpdatedUserDTO(updatedUserInfo.a, updatedUserInfo.b);
    }
}
