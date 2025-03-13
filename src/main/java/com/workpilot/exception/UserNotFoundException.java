package com.workpilot.exception;

public class UserNotFoundException  extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
