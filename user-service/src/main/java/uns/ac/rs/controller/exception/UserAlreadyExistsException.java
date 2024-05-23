package uns.ac.rs.controller.exception;

public class UserAlreadyExistsException extends GenericException {
    public UserAlreadyExistsException() {
        super("User already exists");
    }

    @Override
    public int getErrorCode() {
        return 400;
    }
}
