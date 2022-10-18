package com.evgenykochergin.calendar.model;

import com.evgenykochergin.calendar.model.type.EventStatus;
import com.evgenykochergin.calendar.model.type.EventType;
import com.evgenykochergin.calendar.model.type.Recurrence;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.evgenykochergin.calendar.model.type.EventStatus.*;
import static com.evgenykochergin.calendar.model.type.EventType.RECURRING;
import static com.evgenykochergin.calendar.model.type.EventType.SINGLE;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;

public class Event {
    public final UUID id;
    public final UUID userId;
    public final UUID eventDetailsId;
    public final EventStatus status;
    public final LocalDateTime startDate;
    public final LocalDateTime endDate;
    public final Duration duration;
    public final EventType type;
    public final Optional<Recurrence> recurrence;

    public static Builder event() {
        return new Builder();
    }

    private Event(Builder builder) {
        this.id = requireNonNull(builder.id, "id is required");
        this.userId = requireNonNull(builder.userId, "userId is required");
        this.eventDetailsId = requireNonNull(builder.eventDetailsId, "eventDetailsId is required");
        this.status = requireNonNull(builder.status, "status is required");
        this.startDate = requireNonNull(builder.startDate, "startDate is required");
        this.endDate = requireNonNull(builder.endDate, "endDate is required");
        this.duration = requireNonNull(builder.duration, "duration is required");
        this.type = requireNonNull(builder.type, "type is required");
        this.recurrence = requireNonNull(builder.recurrence, "recurrence is required");

        if (is(SINGLE)) {
            if (recurrence.isPresent()) {
                throw new IllegalArgumentException("recurrence must be empty for single events");
            }
        }
        if (is(RECURRING)) {
            if (recurrence.isEmpty()) {
                throw new IllegalArgumentException("recurrence must be presented for recurring events");
            }
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate should be after endDate");
        }
        if (duration.isNegative()) {
            throw new IllegalArgumentException("duration should be positive");
        }
    }

    public boolean is(EventType type) {
        return this.type == type;
    }

    public boolean is(EventStatus status) {
        return this.status == status;
    }

    public Recurrence recurrence() {
        return recurrence.orElseThrow();
    }

    public Event accept() {
        if (is(ACCEPTED)) {
            return this;
        }
        return copy()
                .status(ACCEPTED)
                .build();
    }

    public Event decline() {
        if (is(DECLINED)) {
            return this;
        }
        return copy()
                .status(DECLINED)
                .build();
    }

    public List<Event> newRecurringInstances(LocalDateTime fromDate, LocalDateTime toDate) {
        if (!is(RECURRING)) {
            throw new IllegalStateException("Only from recurring event can create recurring instances");
        }
        if (this.endDate.isBefore(fromDate) || this.startDate.isAfter(toDate)) {
            return emptyList();
        }
        final var frequency = recurrence().frequency;
        final var instances = new ArrayList<Event>();
        var startDate = this.startDate;
        while (startDate.compareTo(toDate) <= 0 && startDate.compareTo(this.endDate) <= 0) {
            final var instanceStartDate = startDate;
            final var instanceEndDate = startDate.plus(this.duration);
            if ((instanceStartDate.isAfter(fromDate) && instanceStartDate.isBefore(toDate)) || (instanceEndDate.isAfter(fromDate) && instanceEndDate.isBefore(toDate))) {
                instances.add(this.newRecurringInstance(instanceStartDate, instanceEndDate));
            }
            startDate = frequency.nextDate(startDate);
        }
        return instances;
    }

    public Event newRecurringInstance(LocalDateTime instanceStartDate, LocalDateTime instanceEndDate) {
        if (!is(RECURRING)) {
            throw new IllegalStateException("Only from recurring event can create new recurring instances");
        }
        return copy()
                .startDate(instanceStartDate)
                .endDate(instanceEndDate)
                .build();
    }

    private Builder copy() {
        return event()
                .id(this.id)
                .userId(this.userId)
                .eventDetailsId(this.eventDetailsId)
                .status(this.status)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .duration(this.duration)
                .type(this.type)
                .recurrence(this.recurrence);
    }

    public static class Builder {

        private UUID id = randomUUID();
        private UUID userId;
        private UUID eventDetailsId;
        private EventStatus status = PENDING;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Duration duration;
        private EventType type;
        private Optional<Recurrence> recurrence = empty();

        private Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder eventDetailsId(UUID eventDetailsId) {
            this.eventDetailsId = eventDetailsId;
            return this;
        }

        public Builder status(EventStatus status) {
            this.status = status;
            return this;
        }

        public Builder startDate(LocalDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDateTime endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder duration(Duration duration) {
            this.duration = duration;
            return this;
        }

        public Builder type(EventType type) {
            this.type = type;
            return this;
        }

        public Builder recurrence(Optional<Recurrence> recurrence) {
            this.recurrence = recurrence;
            return this;
        }

        public Builder recurrence(Recurrence recurrence) {
            return recurrence(Optional.of(recurrence));
        }

        public Event build() {
            return new Event(this);
        }
    }
}
