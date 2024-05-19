package uns.ac.rs.controller.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<GenericException> {
    @Override
    public Response toResponse(GenericException e) {
        return Response.status(e.getErrorCode())
            .entity(e.getMessage())
            .build();
    }
}
