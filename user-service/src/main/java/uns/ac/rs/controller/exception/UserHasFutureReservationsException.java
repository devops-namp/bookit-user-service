package uns.ac.rs.controller.exception;

public class UserHasFutureReservationsException extends GenericException {
    public UserHasFutureReservationsException() {
        super("User has future reservations");
    }

    @Override
    public int getErrorCode() {
        return 400;
    }
}
