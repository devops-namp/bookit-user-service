package uns.ac.rs.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uns.ac.rs.controller.dto.TokenDTO;
import uns.ac.rs.controller.request.LoginRequest;
import uns.ac.rs.entity.Role;
import uns.ac.rs.security.PasswordEncoder;
import uns.ac.rs.security.TokenUtils;
import uns.ac.rs.service.UserService;

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
    public Response login(LoginRequest loginRequest) {
        var userOptional = userService.findByUsername(loginRequest.getUsername());
        if (userOptional.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        var user = userOptional.get();
        if (user.getPassword().equals(passwordEncoder.encode(loginRequest.getPassword()))) {
            try {
                return Response.ok(new TokenDTO(tokenUtils.generateToken(user.getUsername(), Role.USER))).build();
            } catch (Exception e) {
                e.printStackTrace();
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

}
