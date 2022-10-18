package com.evgenykochergin.calendar.model;

import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public class User {

    public final UUID id;
    public final String username;
    public final String password;

    public static Builder user() {
        return new Builder();
    }

    private User(Builder builder) {
        this.id = requireNonNull(builder.id, "id is required");
        this.username = requireNonNull(builder.username, "username is required");
        this.password = requireNonNull(builder.password, "password is required");
    }

    public static class Builder {

        private UUID id = randomUUID();
        private String username;
        private String password;

        private Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
