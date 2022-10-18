package com.evgenykochergin.calendar.error;

public class UserAlreadyExistsException extends ApplicationException {
    public UserAlreadyExistsException() {
        super("User already exists");
    }
}
