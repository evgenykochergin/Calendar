package com.evgenykochergin.calendar.service;

import com.evgenykochergin.calendar.error.*;
import com.evgenykochergin.calendar.model.Event;
import com.evgenykochergin.calendar.model.EventDetails;
import com.evgenykochergin.calendar.model.type.EventDetailsVisibility;
import com.evgenykochergin.calendar.model.type.Recurrence;
import com.evgenykochergin.calendar.model.type.UserEventStatus;
import com.evgenykochergin.calendar.service.FreeTimeSlotFinder.TimeSlot;
import com.evgenykochergin.calendar.service.mapper.EventDetailsMapper;
import com.evgenykochergin.calendar.service.mapper.EventMapper;
import com.evgenykochergin.calendar.service.unmapper.EventDetailsUnmapper;
import com.evgenykochergin.calendar.service.unmapper.EventUnmapper;
import org.jooq.DSLContext;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static com.evgenykochergin.calendar.db.tables.Event.EVENT;
import static com.evgenykochergin.calendar.db.tables.EventDetails.EVENT_DETAILS;
import static com.evgenykochergin.calendar.model.Event.event;
import static com.evgenykochergin.calendar.model.EventDetails.eventDetails;
import static com.evgenykochergin.calendar.model.type.EventStatus.ACCEPTED;
import static com.evgenykochergin.calendar.model.type.EventStatus.PENDING;
import static com.evgenykochergin.calendar.model.type.EventType.RECURRING;
import static com.evgenykochergin.calendar.model.type.EventType.SINGLE;
import static com.evgenykochergin.calendar.service.FreeTimeSlotFinder.freeTimeSlot;
import static java.lang.String.format;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

public class EventService {
    public static class CreateEventParams {
        public final UUID organizerId;
        public final String name;
        public final LocalDateTime startDate;
        public final Duration duration;
        public final SortedSet<UUID> attendeeIds;
        public final EventDetailsVisibility visibility;
        public final Optional<String> description;
        public final Optional<Recurrence> recurrence;

        private CreateEventParams(Builder builder) {
            this.organizerId = requireNonNull(builder.organizerId, "organizerId is required");
            this.name = requireNonNull(builder.name, "name is required");
            this.startDate = requireNonNull(builder.startDate, "startDate is required");
            this.duration = requireNonNull(builder.duration, "duration is required");
            this.attendeeIds = requireNonNull(builder.attendeeIds, "attendeeIds is required");
            this.visibility = requireNonNull(builder.visibility, "visibility is required");
            this.description = requireNonNull(builder.description, "description is required");
            this.recurrence = requireNonNull(builder.recurrence, "recurrence is required");
        }

        public static Builder createEventParams() {
            return new Builder();
        }

        public static class Builder {
            private UUID organizerId;
            private String name;
            private LocalDateTime startDate;
            private Duration duration;
            private SortedSet<UUID> attendeeIds;
            private EventDetailsVisibility visibility;
            private Optional<String> description = empty();
            private Optional<Recurrence> recurrence = empty();

            private Builder() {
            }

            public Builder organizerId(UUID organizerId) {
                this.organizerId = organizerId;
                return this;
            }

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder startDate(LocalDateTime startDate) {
                this.startDate = startDate;
                return this;
            }

            public Builder duration(Duration duration) {
                this.duration = duration;
                return this;
            }

            public Builder attendeeIds(SortedSet<UUID> attendeeIds) {
                this.attendeeIds = attendeeIds;
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

            public Builder description(String description) {
                return description(Optional.of(description));
            }

            public Builder recurrence(Optional<Recurrence> recurrence) {
                this.recurrence = recurrence;
                return this;
            }

            public Builder recurrence(Recurrence recurrence) {
                return recurrence(Optional.of(recurrence));
            }

            public CreateEventParams build() {
                return new CreateEventParams(this);
            }
        }
    }

    private static final int MAX_USER_EVENTS_PERIOD_IN_DAYS = 365;
    private final DSLContext db;
    private final UserService userService;
    private final EventDetailsMapper eventDetailsMapper;
    private final EventDetailsUnmapper eventDetailsUnmapper;
    private final EventMapper eventMapper;
    private final EventUnmapper eventUnmapper;

    public EventService(DSLContext db, UserService userService) {
        this.db = db;
        this.userService = userService;
        this.eventDetailsMapper = new EventDetailsMapper();
        this.eventDetailsUnmapper = new EventDetailsUnmapper();
        this.eventMapper = new EventMapper();
        this.eventUnmapper = new EventUnmapper();
    }

    public Optional<Event> findEventById(UUID eventId) {
        return db.selectFrom(EVENT)
                .where(EVENT.ID.eq(eventId))
                .fetchOptional(eventMapper);
    }

    public Event getEventById(UUID eventId) {
        return findEventById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
    }

    public Optional<EventDetails> findEventDetailsById(UUID id) {
        return db.selectFrom(EVENT_DETAILS)
                .where(EVENT_DETAILS.ID.eq(id))
                .fetchOptional(eventDetailsMapper);
    }

    public EventDetails getEventDetailsById(UUID id) {
        return findEventDetailsById(id).orElseThrow(() -> new EventDetailsNotFoundException(id));
    }

    public Collection<UserEventStatus> getUserEventStatusesByEventDetailsId(UUID eventDetailsId) {
        return db.selectFrom(EVENT)
                .where(EVENT.EVENT_DETAILS_ID.in(eventDetailsId))
                .fetch(eventMapper)
                .stream()
                .map(event -> new UserEventStatus(event.userId, event.status))
                .collect(toList());
    }

    public Event getEventForUser(UUID eventDetailsId, UUID userId) {
        return db.selectFrom(EVENT)
                .where(EVENT.EVENT_DETAILS_ID.eq(eventDetailsId).and(EVENT.USER_ID.eq(userId)))
                .fetchOne(eventMapper);
    }


    public Event createEvent(CreateEventParams params) {
        validateOrganizerId(params.organizerId);
        validateAttendees(params.attendeeIds, params.organizerId);
        final var eventDetails = eventDetails()
                .organizerId(params.organizerId)
                .name(params.name)
                .description(params.description)
                .visibility(params.visibility)
                .build();
        final var organizerEvent = buildEventFor(params.organizerId, eventDetails.id, params);
        final var attendeeEvents = params.attendeeIds.stream()
                .map(attendeeId -> buildEventFor(attendeeId, eventDetails.id, params))
                .toList();
        db.transaction(tx -> {
            tx.dsl().executeInsert(eventDetailsUnmapper.unmap(eventDetails));
            tx.dsl().batchInsert(concat(Stream.of(organizerEvent), attendeeEvents.stream()).map(eventUnmapper::unmap).toList()).execute();
        });
        return organizerEvent;
    }

    public Event acceptEvent(UUID eventId) {
        final var event = getEventById(eventId).accept();
        db.transaction(tx -> {
            tx.dsl().executeUpdate(eventUnmapper.unmap(event));
        });
        return event;
    }

    public Event declineEvent(UUID eventId) {
        final var event = getEventById(eventId).decline();
        db.transaction(tx -> {
            tx.dsl().executeUpdate(eventUnmapper.unmap(event));
        });
        return event;
    }

    public List<Event> getUserEvents(UUID userId, LocalDateTime fromDate, LocalDateTime toDate) {
        userService.getById(userId);
        if (DAYS.between(fromDate, toDate) > MAX_USER_EVENTS_PERIOD_IN_DAYS) {
            throw new IllegalArgumentException(format("Period should not be more than %s days", MAX_USER_EVENTS_PERIOD_IN_DAYS));
        }
        final var singleEvents = db.selectFrom(EVENT)
                .where(EVENT.USER_ID.eq(userId)
                        .and(EVENT.START_DATE.between(fromDate, toDate).or(EVENT.END_DATE.between(fromDate, toDate)))
                        .and(EVENT.TYPE.eq(SINGLE.name())))
                .fetch(eventMapper);
        final var recurringEvents = db.selectFrom(EVENT)
                .where(EVENT.USER_ID.eq(userId)
                        .and(EVENT.END_DATE.ge(fromDate))
                        .and(EVENT.TYPE.eq(RECURRING.name())))
                .fetch(eventMapper);
        return concat(singleEvents.stream(), recurringEvents.stream().flatMap(event -> event.newRecurringInstances(fromDate, toDate).stream()))
                .sorted(comparing(event -> event.startDate))
                .toList();
    }


    public Optional<TimeSlot> findFreeTimeSlot(Collection<UUID> userIds,
                                               Duration duration,
                                               LocalDateTime fromDate,
                                               LocalDateTime toDate) {
        return freeTimeSlot(
                userIds.stream()
                        .flatMap(userId -> getUserEvents(userId, fromDate, toDate).stream())
                        .map(event -> new TimeSlot(event.startDate, event.endDate))
                        .toList(),
                duration,
                fromDate,
                toDate
        );
    }

    private Event buildEventFor(UUID userId, UUID eventDetailsId, CreateEventParams params) {
        return event()
                .userId(userId)
                .eventDetailsId(eventDetailsId)
                .status(params.organizerId.equals(userId) ? ACCEPTED : PENDING)
                .type(params.recurrence.isPresent() ? RECURRING : SINGLE)
                .duration(params.duration)
                .startDate(params.startDate)
                .endDate(endDate(params))
                .recurrence(params.recurrence)
                .build();
    }

    private LocalDateTime endDate(CreateEventParams params) {
        if (params.recurrence.isEmpty()) {
            return params.startDate.plus(params.duration);
        }
        return params.recurrence.get().endDate;
    }

    private void validateOrganizerId(UUID organizerId) {
        if (userService.findById(organizerId).isEmpty()) {
            throw new UserNotFoundException(organizerId);
        }
    }

    private void validateAttendees(SortedSet<UUID> attendeeIds, UUID organizerId) {
        if (attendeeIds.contains(organizerId)) {
            throw new ValidationException("Attendees should not contain organizer");
        }
        final var attendees = userService.findAllByIds(attendeeIds);
        if (attendees.size() != attendeeIds.size()) {
            final var existingAttendeeIds = attendees.stream()
                    .map(attendee -> attendee.id)
                    .collect(toSet());
            final var missingAttendeeIds = attendeeIds
                    .stream()
                    .filter(attendeeId -> !existingAttendeeIds.contains(attendeeId))
                    .collect(toSet());
            throw new AttendeesNotFoundException(missingAttendeeIds);
        }
    }
}
