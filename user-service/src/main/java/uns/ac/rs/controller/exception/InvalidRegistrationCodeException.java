package uns.ac.rs.controller.exception;

public class InvalidRegistrationCodeException extends GenericException {
    public InvalidRegistrationCodeException() {
        super("Invalid registration code");
    }

    @Override
    public int getErrorCode() {
        return 404;
    }
}
