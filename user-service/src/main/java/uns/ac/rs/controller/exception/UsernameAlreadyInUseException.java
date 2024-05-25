package uns.ac.rs.controller.exception;

public class UsernameAlreadyInUseException extends GenericException {
    public UsernameAlreadyInUseException() {
        super("Username already in use");
    }

    @Override
    public int getErrorCode() {
        return 400;
    }
}
