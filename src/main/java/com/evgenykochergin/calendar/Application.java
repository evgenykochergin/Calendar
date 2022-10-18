package com.evgenykochergin.calendar;

import com.evgenykochergin.calendar.auth.BasicAuthAccessManager;
import com.evgenykochergin.calendar.auth.Principal;
import com.evgenykochergin.calendar.database.DataSourceProvider;
import com.evgenykochergin.calendar.database.DatabaseMigrator;
import com.evgenykochergin.calendar.error.ApplicationException;
import com.evgenykochergin.calendar.error.IncorrectDateRangeException;
import com.evgenykochergin.calendar.model.Event;
import com.evgenykochergin.calendar.model.type.EventDetailsVisibility;
import com.evgenykochergin.calendar.model.type.Recurrence;
import com.evgenykochergin.calendar.service.EventService;
import com.evgenykochergin.calendar.service.UserService;
import com.evgenykochergin.calendar.service.UserService.CreateUserParams;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.json.JavalinJackson;
import io.javalin.validation.ValidationException;
import org.jooq.DSLContext;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.ThreadLocalTransactionProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.UUID;

import static com.evgenykochergin.calendar.auth.BasicAuthAccessManager.principal;
import static com.evgenykochergin.calendar.auth.Role.LOGGED_IN;
import static com.evgenykochergin.calendar.json.EventSerializer.eventJson;
import static com.evgenykochergin.calendar.json.EventSerializer.eventWithDetailsJson;
import static com.evgenykochergin.calendar.json.Json.OBJECT_MAPPER;
import static com.evgenykochergin.calendar.json.TimeSlotSerializer.timeSlotJson;
import static com.evgenykochergin.calendar.json.UserSerializer.userJson;
import static com.evgenykochergin.calendar.model.type.EventDetailsVisibility.PRIVATE;
import static com.evgenykochergin.calendar.service.EventService.CreateEventParams.createEventParams;
import static io.javalin.Javalin.create;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.http.HttpStatus.*;
import static java.time.Duration.ofDays;
import static java.time.Duration.ofMinutes;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.UUID.fromString;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.jooq.SQLDialect.H2;

public class Application {
    private final DatabaseMigrator databaseMigrator;
    private final Javalin javalin;
    public final UserService userService;
    public final EventService eventService;

    public final DSLContext db;

    public static void main(String[] args) {
        new Application().start(7070);
    }

    public Application() {
        final var dataSourceProvider = new DataSourceProvider();
        final var connectionProvider = new DataSourceConnectionProvider(dataSourceProvider.getDataSource());
        final var configuration = new DefaultConfiguration()
                .set(connectionProvider)
                .set(H2)
                .set(new ThreadLocalTransactionProvider(connectionProvider, true));
        this.db = new DefaultDSLContext(configuration);
        this.userService = new UserService(db);
        this.eventService = new EventService(db, userService);
        this.databaseMigrator = new DatabaseMigrator(dataSourceProvider.getDataSource());
        this.javalin = create(config -> {
            config.accessManager(new BasicAuthAccessManager(userService));
            config.jsonMapper(new JavalinJackson(OBJECT_MAPPER));
        }).routes(routes());
        this.javalin.exception(ValidationException.class, (e, ctx) -> {
            ctx.result(e.getErrors().toString());
            ctx.status(BAD_REQUEST);
        });
        this.javalin.exception(ApplicationException.class, (e, ctx) -> {
            ctx.result(e.getMessage());
            ctx.status(CONFLICT);
        });
    }

    public void start(int port) {
        this.databaseMigrator.migrate();
        this.javalin.start(port);
    }

    private EndpointGroup routes() {
        return () -> {
            post("/users", ctx -> {
                final var request = ctx.bodyValidator(CreateUserRequest.class)
                        .getOrThrow(ValidationException::new);
                final var params = new CreateUserParams(request.username, request.password);
                final var user = userService.createUser(params);
                ctx.json(userJson(user));
                ctx.status(CREATED);
            });

            get("/users/{userId}/events", ctx -> {
                final var principal = principal(ctx);
                final var userId = fromString(ctx.pathParam("userId"));
                final var now = LocalDateTime.now();
                final var fromDate = ofNullable(ctx.queryParam("fromDate")).map(LocalDateTime::parse).orElse(now.minus(ofDays(7)));
                final var toDate = ofNullable(ctx.queryParam("toDate")).map(LocalDateTime::parse).orElse(now.plus(ofDays(7)));
                if (!fromDate.isBefore(toDate)) {
                    throw new IncorrectDateRangeException(fromDate, toDate);
                }
                final var events = eventService.getUserEvents(userId, fromDate, toDate);
                ctx.json(events.stream().map(event -> eventJsonFor(event, principal)).toList());
                ctx.status(OK);
            }, LOGGED_IN);

            get("/events/{eventId}", ctx -> {
                final var principal = principal(ctx);
                final var eventId = fromString(ctx.pathParam("eventId"));
                final var event = eventService.getEventById(eventId);
                ctx.json(eventJsonFor(event, principal));
                ctx.status(OK);
            }, LOGGED_IN);

            post("/events", ctx -> {
                final var principal = principal(ctx);
                final var request = ctx.bodyValidator(CreateEventRequest.class)
                        .getOrThrow(ValidationException::new);
                final var params = createEventParams()
                        .organizerId(principal.userId())
                        .name(request.name)
                        .startDate(request.startDate)
                        .duration(ofMinutes(request.duration))
                        .attendeeIds(request.attendeeIds)
                        .visibility(request.visibility)
                        .description(request.description)
                        .recurrence(request.recurrence)
                        .build();
                final var event = eventService.createEvent(params);
                ctx.json(eventJsonFor(event, principal));
                ctx.status(CREATED);
            }, LOGGED_IN);

            post("/events/{eventId}/accept", ctx -> {
                final var eventId = fromString(ctx.pathParam("eventId"));
                eventService.acceptEvent(eventId);
                ctx.status(ACCEPTED);
            }, LOGGED_IN);

            post("/events/{eventId}/decline", ctx -> {
                final var eventId = fromString(ctx.pathParam("eventId"));
                eventService.declineEvent(eventId);
                ctx.status(ACCEPTED);
            }, LOGGED_IN);

            post("/events/free-time-slot", ctx -> {
                final var request = ctx.bodyValidator(FreeTimeSlotRequest.class)
                        .getOrThrow(ValidationException::new);
                final var freeTimeSlot = eventService.findFreeTimeSlot(
                        request.userIds,
                        ofMinutes(request.duration),
                        request.fromDate,
                        request.toDate
                );
                freeTimeSlot.ifPresentOrElse(
                        timeSlot -> {
                            ctx.json(timeSlotJson(timeSlot));
                            ctx.status(OK);
                        },
                        () -> ctx.status(NO_CONTENT)
                );
            }, LOGGED_IN);
        };
    }

    private JsonNode eventJsonFor(Event event, Principal principal) {
        final var userId = principal.userId();
        final var eventDetails = eventService.getEventDetailsById(event.eventDetailsId);
        final var userEventStatuses = eventService.getUserEventStatusesByEventDetailsId(eventDetails.id)
                .stream()
                .collect(toMap(userEventStatus -> userEventStatus.userId, identity()));
        if (eventDetails.is(PRIVATE)) {
            if (!eventDetails.organizedBy(userId) && !userEventStatuses.containsKey(userId)) {
                return eventJson(event);
            }
        }
        return eventWithDetailsJson(event, eventDetails, userEventStatuses.values());
    }

    private static class CreateUserRequest {

        public final String username;
        public final String password;

        @JsonCreator
        public CreateUserRequest(@JsonProperty("username") String username,
                                 @JsonProperty("password") String password) {
            this.username = requireNonNull(username, "username is required");
            this.password = requireNonNull(password, "password is required");
        }
    }

    private static class CreateEventRequest {
        public final String name;
        public final LocalDateTime startDate;
        public final Integer duration;
        public final SortedSet<UUID> attendeeIds;

        public final EventDetailsVisibility visibility;
        public final Optional<String> description;
        public final Optional<Recurrence> recurrence;

        @JsonCreator
        public CreateEventRequest(@JsonProperty("name") String name,
                                  @JsonProperty("startDate") LocalDateTime startDate,
                                  @JsonProperty("duration") Integer duration,
                                  @JsonProperty("attendeeIds") SortedSet<UUID> attendeeIds,
                                  @JsonProperty("visibility") EventDetailsVisibility visibility,
                                  @JsonProperty("description") Optional<String> description,
                                  @JsonProperty("recurrence") Optional<Recurrence> recurrence) {
            this.name = requireNonNull(name, "name is required");
            this.startDate = requireNonNull(startDate, "startDate is required");
            this.duration = requireNonNull(duration, "duration is required");
            this.attendeeIds = requireNonNull(attendeeIds, "attendeeIds is required");
            this.visibility = requireNonNull(visibility, "visibility is required");
            this.description = requireNonNull(description, "description is required");
            this.recurrence = requireNonNull(recurrence, "recurrence is required");
        }
    }

    private static class FreeTimeSlotRequest {

        public final List<UUID> userIds;
        public final Integer duration;
        public final LocalDateTime fromDate;
        public final LocalDateTime toDate;

        @JsonCreator
        public FreeTimeSlotRequest(@JsonProperty("userIds") List<UUID> userIds,
                                   @JsonProperty("duration") Integer duration,
                                   @JsonProperty("fromDate") LocalDateTime fromDate,
                                   @JsonProperty("toDate") LocalDateTime toDate) {
            this.userIds = requireNonNull(userIds, "userIds are required");
            this.duration = requireNonNull(duration, "duration is required");
            this.fromDate = requireNonNull(fromDate, "fromDate is required");
            this.toDate = requireNonNull(toDate, "toDate is required");

            if (userIds.isEmpty()) {
                throw new IllegalArgumentException("userIds can't be empty");
            }
            if (duration < 1) {
                throw new IllegalArgumentException("duration should be greater than 0");
            }
            if (!fromDate.isBefore(toDate)) {
                throw new IncorrectDateRangeException(fromDate, toDate);
            }
        }
    }
}
