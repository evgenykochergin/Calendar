package com.evgenykochergin.calendar.error;

import java.util.UUID;

import static java.lang.String.format;

public class EventNotFoundException extends ApplicationException {

    public EventNotFoundException(UUID eventId) {
        super(format("Event %s not found ", eventId));
    }
}
