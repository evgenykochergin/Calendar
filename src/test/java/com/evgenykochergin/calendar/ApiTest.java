package com.evgenykochergin.calendar;

import com.evgenykochergin.calendar.model.type.EventStatus;
import com.evgenykochergin.calendar.model.type.Recurrence;
import com.evgenykochergin.calendar.service.UserService.CreateUserParams;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

import static com.evgenykochergin.calendar.json.Json.arrayNode;
import static com.evgenykochergin.calendar.json.Json.objectNode;
import static com.evgenykochergin.calendar.model.type.EventDetailsVisibility.PRIVATE;
import static com.evgenykochergin.calendar.model.type.EventDetailsVisibility.PUBLIC;
import static com.evgenykochergin.calendar.model.type.RecurrenceFrequency.DAILY;
import static com.evgenykochergin.calendar.service.EventService.CreateEventParams.createEventParams;
import static io.javalin.http.HttpStatus.ACCEPTED;
import static io.javalin.http.HttpStatus.CREATED;
import static io.restassured.RestAssured.given;
import static java.time.Duration.ofMinutes;
import static java.time.LocalDateTime.parse;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jetty.http.HttpStatus.Code.NO_CONTENT;
import static org.eclipse.jetty.http.HttpStatus.Code.OK;

public class ApiTest extends FunctionalTest {

    @Test
    public void should_create_user() {
        // given
        final var requestBody = objectNode()
                .put("username", "test-user")
                .put("password", "test-password");
        // when
        final var response = given()
                .body(requestBody)
                .when()
                .post("/users")
                .then()
                .extract()
                .response();

        // then
        assertThat(response.statusCode())
                .isEqualTo(CREATED.getCode());
        assertThatJson(response.as(JsonNode.class))
                .isEqualTo(objectNode()
                        .put("id", "${json-unit.ignore}")
                        .put("username", "test-user"));
        assertThat(userService.findByUsername("test-user"))
                .hasValueSatisfying(user -> assertThat(user.password).isEqualTo("test-password"));
    }

    @Test
    public void should_create_single_event() {
        // given
        final var organizer = userService.createUser(new CreateUserParams("organizer", "organizer-password"));
        final var attendee = userService.createUser(new CreateUserParams("attendee", "attendee-password"));
        final var requestBody = objectNode()
                .put("name", "test-name")
                .put("description", "test-description")
                .put("duration", 60)
                .put("startDate", "2022-10-18T05:00:00")
                .put("visibility", "PUBLIC")
                .set("attendeeIds", arrayNode().add(attendee.id.toString()));
        // when
        final var response = given()
                .auth()
                .preemptive()
                .basic(organizer.username, organizer.password)
                .body(requestBody)
                .when()
                .post("/events")
                .then()
                .extract()
                .response();

        // then
        assertThat(response.statusCode())
                .isEqualTo(CREATED.getCode());
        assertThatJson(response.as(JsonNode.class))
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(objectNode()
                        .put("id", "${json-unit.ignore}")
                        .put("startDate", "2022-10-18T05:00")
                        .put("endDate", "2022-10-18T06:00")
                        .put("duration", 60)
                        .put("type", "SINGLE")
                        .set("details", objectNode()
                                .put("name", "test-name")
                                .put("description", "test-description")
                                .put("organizerId", organizer.id.toString())
                                .put("visibility", "PUBLIC")
                                .set("attendees", arrayNode()
                                        .add(objectNode()
                                                .put("userId", organizer.id.toString())
                                                .put("status", "ACCEPTED"))
                                        .add(objectNode()
                                                .put("userId", attendee.id.toString())
                                                .put("status", "PENDING")))));
    }

    @Test
    public void should_create_recurring_event() {
        // given
        final var organizer = userService.createUser(new CreateUserParams("organizer", "organizer-password"));
        final var attendee = userService.createUser(new CreateUserParams("attendee", "attendee-password"));
        final var requestBody = objectNode()
                .put("name", "test-name")
                .put("description", "test-description")
                .put("duration", 60)
                .put("startDate", "2022-10-18T05:00")
                .<ObjectNode>set("recurrence", objectNode()
                        .put("frequency", "DAILY")
                        .put("endDate", "2022-10-20T05:00:00"))
                .put("visibility", "PUBLIC")
                .set("attendeeIds", arrayNode().add(attendee.id.toString()));
        // when
        final var response = given()
                .auth()
                .preemptive()
                .basic(organizer.username, organizer.password)
                .body(requestBody)
                .when()
                .post("/events")
                .then()
                .extract()
                .response();

        // then
        assertThat(response.statusCode())
                .isEqualTo(CREATED.getCode());
        assertThatJson(response.as(JsonNode.class))
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(objectNode()
                        .put("id", "${json-unit.ignore}")
                        .put("startDate", "2022-10-18T05:00")
                        .put("endDate", "2022-10-18T06:00")
                        .put("duration", 60)
                        .put("type", "RECURRING")
                        .<ObjectNode>set("recurrence", objectNode()
                                .put("frequency", "DAILY")
                                .put("endDate", "2022-10-20T05:00"))
                        .set("details", objectNode()
                                .put("name", "test-name")
                                .put("description", "test-description")
                                .put("organizerId", organizer.id.toString())
                                .put("visibility", "PUBLIC")
                                .set("attendees", arrayNode()
                                        .add(objectNode()
                                                .put("userId", organizer.id.toString())
                                                .put("status", "ACCEPTED"))
                                        .add(objectNode()
                                                .put("userId", attendee.id.toString())
                                                .put("status", "PENDING")))));
    }

    @Test
    public void should_accept_event() {
        // given
        final var organizer = userService.createUser(new CreateUserParams("organizer", "organizer-password"));
        final var attendee = userService.createUser(new CreateUserParams("attendee", "attendee-password"));
        final var eventForOrganizer = eventService.createEvent(
                createEventParams()
                        .organizerId(organizer.id)
                        .name("single")
                        .startDate(date("2022-10-18T05:00"))
                        .duration(ofMinutes(60))
                        .attendeeIds(new TreeSet<>(List.of(attendee.id)))
                        .visibility(PUBLIC)
                        .build());
        final var eventForAttendee = eventService.getEventForUser(eventForOrganizer.eventDetailsId, attendee.id);

        // when
        final var response = given()
                .auth()
                .preemptive()
                .basic(attendee.username, attendee.password)
                .when()
                .post("/events/{eventId}/accept", eventForAttendee.id)
                .then()
                .extract()
                .response();

        // then
        assertThat(response.statusCode())
                .isEqualTo(ACCEPTED.getCode());
        assertThat(eventService.getEventById(eventForAttendee.id).status)
                .isEqualTo(EventStatus.ACCEPTED);
    }

    @Test
    public void should_decline_event() {
        // given
        final var organizer = userService.createUser(new CreateUserParams("organizer", "organizer-password"));
        final var attendee = userService.createUser(new CreateUserParams("attendee", "attendee-password"));
        final var eventForOrganizer = eventService.createEvent(
                createEventParams()
                        .organizerId(organizer.id)
                        .name("single")
                        .startDate(date("2022-10-18T05:00"))
                        .duration(ofMinutes(60))
                        .attendeeIds(new TreeSet<>(List.of(attendee.id)))
                        .visibility(PUBLIC)
                        .build());
        final var eventForAttendee = eventService.getEventForUser(eventForOrganizer.eventDetailsId, attendee.id);

        // when
        final var response = given()
                .auth()
                .preemptive()
                .basic(attendee.username, attendee.password)
                .when()
                .post("/events/{eventId}/decline", eventForAttendee.id)
                .then()
                .extract()
                .response();

        // then
        assertThat(response.statusCode())
                .isEqualTo(ACCEPTED.getCode());
        assertThat(eventService.getEventById(eventForAttendee.id).status)
                .isEqualTo(EventStatus.DECLINED);
    }

    @Test
    public void should_get_user_events() {
        // given
        final var organizer = userService.createUser(new CreateUserParams("organizer", "organizer-password"));
        final var attendee = userService.createUser(new CreateUserParams("attendee", "attendee-password"));
        final var singleEvent = eventService.createEvent(
                createEventParams()
                        .organizerId(organizer.id)
                        .name("single")
                        .startDate(date("2022-10-18T05:00"))
                        .duration(ofMinutes(60))
                        .attendeeIds(new TreeSet<>(List.of(attendee.id)))
                        .visibility(PUBLIC)
                        .build());
        final var recurringEvent = eventService.createEvent(
                createEventParams()
                        .organizerId(organizer.id)
                        .name("recurring")
                        .startDate(date("2022-10-17T10:00"))
                        .duration(ofMinutes(30))
                        .attendeeIds(new TreeSet<>(List.of(attendee.id)))
                        .visibility(PUBLIC)
                        .recurrence(Optional.of(new Recurrence(DAILY, date("2022-11-17T10:00"))))
                        .build());
        // when
        final var response = given()
                .auth()
                .preemptive()
                .basic(organizer.username, organizer.password)
                .queryParam("fromDate", "2022-10-16T00:00")
                .queryParam("toDate", "2022-10-19T00:00")
                .when()
                .get("/users/{userId}/events", organizer.id)
                .then()
                .extract()
                .response();

        // then
        assertThat(response.statusCode())
                .isEqualTo(OK.getCode());
        assertThatJson(response.as(JsonNode.class))
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(arrayNode()
                        .add(objectNode()
                                .put("id", recurringEvent.id.toString())
                                .put("startDate", "2022-10-17T10:00")
                                .put("endDate", "2022-10-17T10:30")
                                .put("duration", 30)
                                .put("type", "RECURRING")
                                .<ObjectNode>set("recurrence", objectNode()
                                        .put("frequency", "DAILY")
                                        .put("endDate", "2022-11-17T10:00"))
                                .set("details", objectNode()
                                        .put("name", "recurring")
                                        .put("organizerId", organizer.id.toString())
                                        .put("visibility", "PUBLIC")
                                        .set("attendees", arrayNode()
                                                .add(objectNode()
                                                        .put("userId", organizer.id.toString())
                                                        .put("status", "ACCEPTED"))
                                                .add(objectNode()
                                                        .put("userId", attendee.id.toString())
                                                        .put("status", "PENDING")))))
                        .add(objectNode()
                                .put("id", singleEvent.id.toString())
                                .put("startDate", "2022-10-18T05:00")
                                .put("endDate", "2022-10-18T06:00")
                                .put("duration", 60)
                                .put("type", "SINGLE")
                                .set("details", objectNode()
                                        .put("name", "single")
                                        .put("organizerId", organizer.id.toString())
                                        .put("visibility", "PUBLIC")
                                        .set("attendees", arrayNode()
                                                .add(objectNode()
                                                        .put("userId", organizer.id.toString())
                                                        .put("status", "ACCEPTED"))
                                                .add(objectNode()
                                                        .put("userId", attendee.id.toString())
                                                        .put("status", "PENDING")))))
                        .add(objectNode()
                                .put("id", recurringEvent.id.toString())
                                .put("startDate", "2022-10-18T10:00")
                                .put("endDate", "2022-10-18T10:30")
                                .put("duration", 30)
                                .put("type", "RECURRING")
                                .<ObjectNode>set("recurrence", objectNode()
                                        .put("frequency", "DAILY")
                                        .put("endDate", "2022-11-17T10:00"))
                                .set("details", objectNode()
                                        .put("name", "recurring")
                                        .put("organizerId", organizer.id.toString())
                                        .put("visibility", "PUBLIC")
                                        .set("attendees", arrayNode()
                                                .add(objectNode()
                                                        .put("userId", organizer.id.toString())
                                                        .put("status", "ACCEPTED"))
                                                .add(objectNode()
                                                        .put("userId", attendee.id.toString())
                                                        .put("status", "PENDING"))))));
    }

    @Test
    public void should_get_user_event_with_hidden_details_when_event_is_private() {
        // given
        final var organizer = userService.createUser(new CreateUserParams("organizer", "organizer-password"));
        final var attendee = userService.createUser(new CreateUserParams("attendee", "attendee-password"));
        final var user = userService.createUser(new CreateUserParams("user", "user-password"));
        final var singleEvent = eventService.createEvent(
                createEventParams()
                        .organizerId(organizer.id)
                        .name("single")
                        .startDate(date("2022-10-18T05:00"))
                        .duration(ofMinutes(60))
                        .attendeeIds(new TreeSet<>(List.of(attendee.id)))
                        .visibility(PRIVATE)
                        .build());
        // when
        final var response = given()
                .auth()
                .preemptive()
                .basic(user.username, user.password)
                .queryParam("fromDate", "2022-10-16T00:00")
                .queryParam("toDate", "2022-10-19T00:00")
                .when()
                .get("/users/{userId}/events", organizer.id)
                .then()
                .extract()
                .response();

        // then
        assertThat(response.statusCode())
                .isEqualTo(OK.getCode());
        assertThatJson(response.as(JsonNode.class))
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(arrayNode()
                        .add(objectNode()
                                .put("id", singleEvent.id.toString())
                                .put("startDate", "2022-10-18T05:00")
                                .put("endDate", "2022-10-18T06:00")
                                .put("duration", 60)
                                .put("type", "SINGLE")));
    }

    @Test
    public void should_find_free_time_slot() {
        // given
        final var user1 = userService.createUser(new CreateUserParams("user1", "password"));
        final var user2 = userService.createUser(new CreateUserParams("user2", "password"));
        eventService.createEvent(
                createEventParams()
                        .organizerId(user1.id)
                        .name("single")
                        .startDate(date("2022-10-17T12:30"))
                        .duration(ofMinutes(60))
                        .attendeeIds(new TreeSet<>(List.of(user2.id)))
                        .visibility(PUBLIC)
                        .build());
        eventService.createEvent(
                createEventParams()
                        .organizerId(user2.id)
                        .name("recurring")
                        .startDate(date("2022-10-17T10:00"))
                        .duration(ofMinutes(120))
                        .attendeeIds(new TreeSet<>(List.of(user1.id)))
                        .visibility(PUBLIC)
                        .recurrence(Optional.of(new Recurrence(DAILY, date("2022-11-17T10:00"))))
                        .build());
        final var requestBody = objectNode()
                .<ObjectNode>set("userIds", arrayNode().add(user1.id.toString()).add(user2.id.toString()))
                .put("duration", 60)
                .put("fromDate", "2022-10-17T09:30")
                .put("toDate", "2022-10-18T09:30");

        // when
        final var response = given()
                .auth()
                .preemptive()
                .basic(user1.username, user1.password)
                .body(requestBody)
                .when()
                .post("/events/free-time-slot")
                .then()
                .extract()
                .response();

        // then
        assertThat(response.statusCode())
                .isEqualTo(OK.getCode());
        assertThatJson(response.as(JsonNode.class))
                .isEqualTo(objectNode()
                        .put("startDate", "2022-10-17T13:30")
                        .put("endDate", "2022-10-17T14:30"));
    }

    @Test
    public void should_not_find_free_time_slot() {
        // given
        final var user1 = userService.createUser(new CreateUserParams("user1", "password"));
        final var user2 = userService.createUser(new CreateUserParams("user2", "password"));
        eventService.createEvent(
                createEventParams()
                        .organizerId(user1.id)
                        .name("single")
                        .startDate(date("2022-10-17T12:30"))
                        .duration(ofMinutes(60))
                        .attendeeIds(new TreeSet<>(List.of(user2.id)))
                        .visibility(PUBLIC)
                        .build());
        eventService.createEvent(
                createEventParams()
                        .organizerId(user2.id)
                        .name("recurring")
                        .startDate(date("2022-10-17T10:00"))
                        .duration(ofMinutes(120))
                        .attendeeIds(new TreeSet<>(List.of(user1.id)))
                        .visibility(PUBLIC)
                        .recurrence(Optional.of(new Recurrence(DAILY, date("2022-11-17T10:00"))))
                        .build());
        final var requestBody = objectNode()
                .<ObjectNode>set("userIds", arrayNode().add(user1.id.toString()).add(user2.id.toString()))
                .put("duration", 60)
                .put("fromDate", "2022-10-17T09:30")
                .put("toDate", "2022-10-17T14:00");

        // when
        final var response = given()
                .auth()
                .preemptive()
                .basic(user1.username, user1.password)
                .body(requestBody)
                .when()
                .post("/events/free-time-slot")
                .then()
                .extract()
                .response();

        // then
        assertThat(response.statusCode())
                .isEqualTo(NO_CONTENT.getCode());
    }

    private static LocalDateTime date(String string) {
        return parse(string);
    }
}
