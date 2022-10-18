package com.evgenykochergin.calendar.model;

import com.evgenykochergin.calendar.model.type.EventDetailsVisibility;

import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;

public class EventDetails {
    public final UUID id;
    public final UUID organizerId;
    public final String name;
    public final EventDetailsVisibility visibility;
    public final Optional<String> description;

    public static Builder eventDetails() {
        return new Builder();
    }

    public boolean is(EventDetailsVisibility visibility) {
        return this.visibility == visibility;
    }


    private EventDetails(Builder builder) {
        this.id = requireNonNull(builder.id, "id is required");
        this.organizerId = requireNonNull(builder.organizerId, "organizerId is required");
        this.name = requireNonNull(builder.name, "name is required");
        this.visibility = requireNonNull(builder.visibility, "visibility is required");
        this.description = requireNonNull(builder.description, "description is required");
    }

    public boolean organizedBy(UUID userId) {
        return organizerId.equals(userId);
    }

    public static class Builder {

        private UUID id = randomUUID();
        private UUID organizerId;
        private String name;
        private EventDetailsVisibility visibility;
        private Optional<String> description = empty();

        private Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder organizerId(UUID organizerId) {
            this.organizerId = organizerId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder visibility(EventDetailsVisibility visibility) {
            this.visibility = visibility;
            return this;
        }

        public Builder description(Optional<String> description) {
            this.description = description;
            return this;
        }

        public EventDetails build() {
            return new EventDetails(this);
        }
    }
}
