package com.evgenykochergin.calendar.error;

import java.util.UUID;

import static java.lang.String.format;

public class EventDetailsNotFoundException extends ApplicationException {

    public EventDetailsNotFoundException(UUID eventDetailsId) {
        super(format("EventDetails %s not found ", eventDetailsId));
    }
}
