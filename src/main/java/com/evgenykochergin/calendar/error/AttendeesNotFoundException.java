package com.evgenykochergin.calendar.error;

import java.util.Collection;
import java.util.UUID;

import static java.lang.String.format;

public class AttendeesNotFoundException extends ApplicationException {

    public AttendeesNotFoundException(Collection<UUID> attendeeIds) {
        super(format("Attendees %s not found ", attendeeIds));
    }
}
