package com.evgenykochergin.calendar.model.type;

import java.util.UUID;

public class UserEventStatus {

    public final UUID userId;
    public final EventStatus status;

    public UserEventStatus(UUID userId, EventStatus status) {
        this.userId = userId;
        this.status = status;
    }
}
