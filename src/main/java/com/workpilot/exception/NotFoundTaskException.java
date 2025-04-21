package com.workpilot.exception;

public class NotFoundTaskException extends Exception {
    private static final long serialVersionUID = 1L;

    public NotFoundTaskException(String message) {
        super(message);
    }

}
