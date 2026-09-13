package dev.mstefanov.hms.exception;

/** Thrown when a requested entity does not exist. Rendered as 404 by the web and API exception handlers. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(String entity, Object id) {
        return new NotFoundException(entity + " " + id + " not found");
    }
}
