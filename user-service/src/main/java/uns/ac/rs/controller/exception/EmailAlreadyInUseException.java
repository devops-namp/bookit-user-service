package uns.ac.rs.controller.exception;

public class EmailAlreadyInUseException extends GenericException {
    public EmailAlreadyInUseException() {
        super("Email already in use");
    }

    @Override
    public int getErrorCode() {
        return 400;
    }
}
