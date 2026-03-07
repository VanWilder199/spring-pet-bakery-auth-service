package buloshnaya.authService.web;

public class EmailAlreadyExistsException  extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
