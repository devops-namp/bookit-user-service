package uns.ac.rs.controller.exception;

public class GenericUnauthorizedException extends GenericException {

    public GenericUnauthorizedException() {
        super("User not authorized to perform this action");
    }

    @Override
    public int getErrorCode() {
        return 403;
    }
}
