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
import jakarta.ws.rs.core.Response;
import io.vertx.core.json.JsonObject;


import org.jboss.resteasy.reactive.ResponseStatus;

import uns.ac.rs.controller.dto.UpdatedUserDTO;
import uns.ac.rs.controller.dto.UserDTO;
import uns.ac.rs.controller.events.AutoApproveEvent;
import uns.ac.rs.controller.request.ConfirmRegistrationRequest;
import uns.ac.rs.controller.request.ProfileUpdateRequest;
import uns.ac.rs.controller.request.RegistrationRequest;
import uns.ac.rs.entity.RegistrationInfo;
import uns.ac.rs.service.UserService;

import java.util.List;
import java.util.Map;

@Path("/users")
public class UserController {

    @Inject
    UserService userService;



    @Inject
    @Channel("delete-accommodation-queue")
    Emitter<String> deleteAccommodationEmitter;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return "users";
    }


    @Incoming("autoapprove-acc-to-user-queue")
    public void getAutoapprove(JsonObject json) {
        AutoApproveEvent event = json.mapTo(AutoApproveEvent.class);
        if (event.getType().equals(AutoApproveEvent.AutoApproveEventType.GET_BY_USER)) {
            userService.sendAutoapprove(event);
        }
        else if (event.getType().equals(AutoApproveEvent.AutoApproveEventType.INCREMENT)){
            System.out.println("UHVATIO SAM DOGADJAJ");
            userService.incrementCounter(event.getUsername());
        }
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

    @DELETE
    @Path("/{username}")
    @RolesAllowed({ "GUEST", "HOST" })
    @ResponseStatus(204)
    public void deleteAccount(@PathParam("username") String username) {
        var result = userService.deleteProfile(username);
        if (result) {
            deleteAccommodationEmitter.send(username);
        }
    }

    @POST
    @Path("/autoapproveFalse/{username}")
    @PermitAll
    @ResponseStatus(201)
    public void setAutoapproveFalse(@PathParam("username") String username) {
        userService.changeAutoapprove(username, false);
    }

    @POST
    @Path("/autoapproveTrue/{username}")
    @PermitAll
    @ResponseStatus(201)
    public void setAutoapproveTrue(@PathParam("username") String username) {
        userService.changeAutoapprove(username, true);
    }

    @GET
    @Path("/getAutoapproveStatus/{username}")
    @PermitAll
    public Response getAutoapproveStatus(@PathParam("username") String username) {
        boolean b = userService.getAutoapprove(username);
        return Response.ok(b).build();
    }

    @POST
    @Path("/getRejectCount")
    @PermitAll
    public Response getRejectCount(List<String> usernames) {
        Map<String, Integer> rejectCounts = userService.getRejectCounts(usernames);
        return Response.ok(rejectCounts).build();
    }
}
