package uns.ac.rs.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.ResponseStatus;
import uns.ac.rs.controller.dto.TokenDTO;
import uns.ac.rs.controller.request.ChangePasswordRequest;
import uns.ac.rs.controller.request.LoginRequest;
import uns.ac.rs.security.PasswordEncoder;
import uns.ac.rs.security.TokenUtils;
import uns.ac.rs.service.UserService;

import java.util.HashSet;
import java.util.Set;

@Path("/auth")
public class AuthController {

    @Inject
    PasswordEncoder passwordEncoder;
    @Inject
    TokenUtils tokenUtils;
    @Inject
    UserService userService;

    @POST
    @Path("/login")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(@Valid LoginRequest loginRequest) {
        var userOptional = userService.findByUsername(loginRequest.getUsername());
        if (userOptional.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        var user = userOptional.get();
        if (user.getPassword().equals(passwordEncoder.encode(loginRequest.getPassword()))) {
            try {
                return Response.ok(new TokenDTO(tokenUtils.generateToken(user.getUsername(), user.getRole()))).build();
            } catch (Exception e) {
                e.printStackTrace();
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @PUT
    @Path("/password")
    @RolesAllowed({ "GUEST", "HOST" })
    @ResponseStatus(200)
    public void changePassword(@Valid ChangePasswordRequest request) {
        userService.changePassword(request.getUsername(), request.getCurrentPassword(), request.getNewPassword());
    }

    @GET
    @Path("/validate-token")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateToken(@HeaderParam("Authorization") String token) {
        if (token == null || token.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        boolean isValid = tokenUtils.validateToken(token);
        if (isValid) {
            System.out.println("TOKEN JE VALIDAN");
            return Response.ok().build();
        } else {
            System.out.println("TOKEN NIJE VALIDAN");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

    }
}
