package uns.ac.rs.controller.exception;

public class UserDoesNotExistException extends GenericException {
    public UserDoesNotExistException() {
        super("User does not exist");
    }

    @Override
    public int getErrorCode() {
        return 404;
    }
}
