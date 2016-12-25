package br.com.versalius.akijob.utils;

/**
 * Created by Giovanne on 30/06/2016.
 */
public class CustomException extends Exception {
    public CustomException(String message) {
        super(message);
    }

    public CustomException(Throwable cause) {
        super(cause);
    }

    public CustomException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getMessage() {
        return super.getMessage();
    }
}
