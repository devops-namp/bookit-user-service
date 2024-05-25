package uns.ac.rs.controller.exception;

public class PasswordDoesNotMatchException extends GenericException {

    public PasswordDoesNotMatchException() {
        super("Password does not match");
    }

    @Override
    public int getErrorCode() {
        return 400;
    }
}
