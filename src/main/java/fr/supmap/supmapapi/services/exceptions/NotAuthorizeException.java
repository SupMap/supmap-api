package fr.supmap.supmapapi.services.exceptions;

public class NotAuthorizeException extends RuntimeException {
    public NotAuthorizeException(String message) {
        super(message);
    }
}
