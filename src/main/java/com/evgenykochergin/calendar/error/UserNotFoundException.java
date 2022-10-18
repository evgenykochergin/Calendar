package com.evgenykochergin.calendar.error;

import java.util.UUID;

import static java.lang.String.format;

public class UserNotFoundException extends ApplicationException {

    public UserNotFoundException(UUID userId) {
        super(format("User %s not found ", userId));
    }
}
