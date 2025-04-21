package com.workpilot.exception;

public class AssignedProjectAlreadyExistsException extends RuntimeException {
    public AssignedProjectAlreadyExistsException(String message) {
        super(message);
    }
}
