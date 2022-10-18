package com.evgenykochergin.calendar.auth;

import java.util.UUID;

import static java.util.Objects.requireNonNull;

public record Principal(UUID userId, String username) {

    public Principal(UUID userId, String username) {
        this.userId = requireNonNull(userId, "userId is required");
        this.username = requireNonNull(username, "username is required");
    }
}
