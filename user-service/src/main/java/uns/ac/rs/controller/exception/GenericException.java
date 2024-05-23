package uns.ac.rs.controller.exception;

public abstract class GenericException extends RuntimeException {
    public GenericException(String message) {
        super(message);
    }

    public abstract int getErrorCode();
}
