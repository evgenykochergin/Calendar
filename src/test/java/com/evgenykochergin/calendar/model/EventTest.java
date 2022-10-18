package com.evgenykochergin.calendar.model;

import com.evgenykochergin.calendar.model.type.Recurrence;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.evgenykochergin.calendar.model.Event.event;
import static com.evgenykochergin.calendar.model.type.EventStatus.*;
import static com.evgenykochergin.calendar.model.type.EventType.RECURRING;
import static com.evgenykochergin.calendar.model.type.EventType.SINGLE;
import static com.evgenykochergin.calendar.model.type.RecurrenceFrequency.DAILY;
import static java.time.Duration.ofMinutes;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventTest {

    @Test
    void should_create_single_event() {
        // given
        final var userId = randomUUID();
        final var eventDetailsId = randomUUID();
        final var type = SINGLE;
        final var duration = ofMinutes(60);
        final var startDate = LocalDateTime.parse("2022-10-18T05:00");
        final var endDate = startDate.plus(duration);

        // when
        final var event = event()
                .userId(userId)
                .eventDetailsId(eventDetailsId)
                .type(type)
                .duration(duration)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        // then
        assertThat(event.userId).isEqualTo(userId);
        assertThat(event.eventDetailsId).isEqualTo(eventDetailsId);
        assertThat(event.type).isEqualTo(type);
        assertThat(event.duration).isEqualTo(duration);
        assertThat(event.startDate).isEqualTo(startDate);
        assertThat(event.endDate).isEqualTo(endDate);
        assertThat(event.status).isEqualTo(PENDING);
    }

    @Test
    void should_create_recurring_event() {
        // given
        final var userId = randomUUID();
        final var eventDetailsId = randomUUID();
        final var type = RECURRING;
        final var duration = ofMinutes(60);
        final var startDate = LocalDateTime.parse("2022-10-18T05:00");
        final var endDate = LocalDateTime.parse("2022-11-18T05:00");
        final var recurrence = new Recurrence(DAILY, endDate);

        // when
        final var event = event()
                .userId(userId)
                .eventDetailsId(eventDetailsId)
                .type(type)
                .duration(duration)
                .startDate(startDate)
                .endDate(endDate)
                .recurrence(recurrence)
                .build();

        // then
        assertThat(event.userId).isEqualTo(userId);
        assertThat(event.eventDetailsId).isEqualTo(eventDetailsId);
        assertThat(event.type).isEqualTo(type);
        assertThat(event.duration).isEqualTo(duration);
        assertThat(event.startDate).isEqualTo(startDate);
        assertThat(event.endDate).isEqualTo(endDate);
        assertThat(event.recurrence).contains(recurrence);
        assertThat(event.status).isEqualTo(PENDING);
    }

    @Test
    void should_accept_event() {
        // given
        final var event = event()
                .userId(randomUUID())
                .eventDetailsId(randomUUID())
                .type(SINGLE)
                .duration(ofMinutes(60))
                .startDate(LocalDateTime.parse("2022-10-18T05:00"))
                .endDate(LocalDateTime.parse("2022-10-18T06:00"))
                .build();

        // when
        final var acceptedEvent = event.accept();

        // then
        assertThat(acceptedEvent.status).isEqualTo(ACCEPTED);
    }

    @Test
    void should_decline_event() {
        // given
        final var event = event()
                .userId(randomUUID())
                .eventDetailsId(randomUUID())
                .type(SINGLE)
                .duration(ofMinutes(60))
                .startDate(LocalDateTime.parse("2022-10-18T05:00"))
                .endDate(LocalDateTime.parse("2022-10-18T06:00"))
                .build();

        // when
        final var acceptedEvent = event.decline();

        // then
        assertThat(acceptedEvent.status).isEqualTo(DECLINED);
    }

    @Test
    void should_fail_recurring_event_creation_when_recurrence_is_missing() {
        // then
        assertThrows(IllegalArgumentException.class, () -> {
            event()
                    .userId(randomUUID())
                    .eventDetailsId(randomUUID())
                    .type(RECURRING)
                    .duration(ofMinutes(60))
                    .startDate(LocalDateTime.parse("2022-10-18T05:00"))
                    .endDate(LocalDateTime.parse("2022-10-18T06:00"))
                    .build();
        });
    }

    @Test
    void should_fail_single_event_creation_when_recurrence_is_specified() {
        // then
        assertThrows(IllegalArgumentException.class, () -> {
            event()
                    .userId(randomUUID())
                    .eventDetailsId(randomUUID())
                    .type(SINGLE)
                    .duration(ofMinutes(60))
                    .startDate(LocalDateTime.parse("2022-10-18T05:00"))
                    .endDate(LocalDateTime.parse("2022-10-18T06:00"))
                    .recurrence(new Recurrence(DAILY, LocalDateTime.parse("2022-10-18T06:00")))
                    .build();
        });
    }

    @Test
    void should_fail_event_creation_when_duration_is_negative() {
        // then
        assertThrows(IllegalArgumentException.class, () -> {
            event()
                    .userId(randomUUID())
                    .eventDetailsId(randomUUID())
                    .type(SINGLE)
                    .duration(ofMinutes(-60))
                    .startDate(LocalDateTime.parse("2022-10-18T05:00"))
                    .endDate(LocalDateTime.parse("2022-10-18T06:00"))
                    .build();
        });
    }

    @Test
    void should_fail_event_creation_when_start_date_is_not_before_end_date() {
        // then
        assertThrows(IllegalArgumentException.class, () -> {
            event()
                    .userId(randomUUID())
                    .eventDetailsId(randomUUID())
                    .type(SINGLE)
                    .duration(ofMinutes(60))
                    .startDate(LocalDateTime.parse("2022-10-18T06:00"))
                    .endDate(LocalDateTime.parse("2022-10-18T05:00"))
                    .build();
        });
    }

    @Test
    void should_create_new_recurring_instance() {
        // given
        final var recurringEvent = event()
                .userId(randomUUID())
                .eventDetailsId(randomUUID())
                .type(RECURRING)
                .duration(ofMinutes(60))
                .startDate(LocalDateTime.parse("2022-10-18T05:00"))
                .endDate(LocalDateTime.parse("2022-10-18T06:00"))
                .recurrence(new Recurrence(DAILY, LocalDateTime.parse("2022-10-20T06:00")))
                .build();
        final var instanceStartDate = LocalDateTime.parse("2022-10-19T05:00");
        final var instanceEndDate = LocalDateTime.parse("2022-10-19T05:00");

        // when
        final var recurringEventInstance = recurringEvent.newRecurringInstance(instanceStartDate, instanceEndDate);

        // then
        assertThat(recurringEventInstance.startDate).isEqualTo(instanceStartDate);
        assertThat(recurringEventInstance.endDate).isEqualTo(instanceEndDate);
    }

    @Test
    void should_fail_new_recurring_instance_when_event_is_single() {
        // given
        final var recurringEvent = event()
                .userId(randomUUID())
                .eventDetailsId(randomUUID())
                .type(SINGLE)
                .duration(ofMinutes(60))
                .startDate(LocalDateTime.parse("2022-10-18T05:00"))
                .endDate(LocalDateTime.parse("2022-10-18T06:00"))
                .build();
        final var instanceStartDate = LocalDateTime.parse("2022-10-19T05:00");
        final var instanceEndDate = LocalDateTime.parse("2022-10-19T05:00");
        // then
        assertThrows(IllegalStateException.class, () -> recurringEvent.newRecurringInstance(instanceStartDate, instanceEndDate));
    }

    @Test
    void should_create_new_recurring_instances_for_given_date_range() {
        // given
        final var fromDate = LocalDateTime.parse("2022-10-15T00:00");
        final var endDate = LocalDateTime.parse("2022-10-20T00:00");
        final var recurringEvent = event()
                .userId(randomUUID())
                .eventDetailsId(randomUUID())
                .type(RECURRING)
                .duration(ofMinutes(60))
                .startDate(LocalDateTime.parse("2022-10-18T05:00"))
                .endDate(LocalDateTime.parse("2022-10-20T06:00"))
                .recurrence(new Recurrence(DAILY, LocalDateTime.parse("2022-10-20T06:00")))
                .build();


        // when
        final var recurringEventInstances = recurringEvent.newRecurringInstances(fromDate, endDate);

        // then
        assertThat(recurringEventInstances).satisfiesExactly(
                event -> {
                    assertThat(event.startDate).isEqualTo(LocalDateTime.parse("2022-10-18T05:00"));
                    assertThat(event.endDate).isEqualTo(LocalDateTime.parse("2022-10-18T06:00"));
                },
                event -> {
                    assertThat(event.startDate).isEqualTo(LocalDateTime.parse("2022-10-19T05:00"));
                    assertThat(event.endDate).isEqualTo(LocalDateTime.parse("2022-10-19T06:00"));
                }
        );
    }
}