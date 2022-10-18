package com.evgenykochergin.calendar.service;

import com.evgenykochergin.calendar.service.FreeTimeSlotFinder.TimeSlot;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static com.evgenykochergin.calendar.service.FreeTimeSlotFinder.freeTimeSlot;
import static java.time.Duration.ofMinutes;
import static java.time.LocalDateTime.parse;
import static org.assertj.core.api.Assertions.assertThat;

class FreeTimeSlotFinderTest {

    @Test
    void should_return_free_time_slot_when_list_is_empty_and_slot_does_not_exceed_to_date() {
        // when
        final var freeTimeSlot = freeTimeSlot(
                List.of(),
                ofMinutes(30),
                date("2022-10-19T00:00:00"),
                date("2022-10-19T12:00:00")
        );

        // then
        assertThat(freeTimeSlot).contains(
                new TimeSlot(date("2022-10-19T00:00:00"), date("2022-10-19T00:30:00"))
        );
    }

    @Test
    void should_return_nothing_when_list_is_empty_and_slot_exceeds_to_date() {
        // when
        final var freeTimeSlot = freeTimeSlot(
                List.of(),
                ofMinutes(120),
                date("2022-10-19T00:00:00"),
                date("2022-10-19T01:30:00")
        );

        // then
        assertThat(freeTimeSlot).isEmpty();
    }

    @Test
    void should_return_free_time_slot_after_single_busy_time_slot() {
        // when
        final var freeTimeSlot = freeTimeSlot(
                List.of(new TimeSlot(date("2022-10-19T00:00:00"), date("2022-10-19T00:30:00"))),
                ofMinutes(120),
                date("2022-10-19T00:00:00"),
                date("2022-10-19T04:00:00")
        );

        // then
        assertThat(freeTimeSlot).contains(
                new TimeSlot(date("2022-10-19T00:30:00"), date("2022-10-19T02:30:00"))
        );
    }

    @Test
    void should_return_free_time_slot_before_single_busy_time_slot() {
        // when
        final var freeTimeSlot = freeTimeSlot(
                List.of(new TimeSlot(date("2022-10-19T02:00:00"), date("2022-10-19T02:30:00"))),
                ofMinutes(120),
                date("2022-10-19T00:00:00"),
                date("2022-10-19T04:00:00")
        );

        // then
        assertThat(freeTimeSlot).contains(
                new TimeSlot(date("2022-10-19T00:00:00"), date("2022-10-19T02:00:00"))
        );
    }

    @Test
    void should_return_nothing_when_list_has_single_busy_time_slot_and_free_time_slot_exceeds_to_date() {
        // when
        final var freeTimeSlot = freeTimeSlot(
                List.of(new TimeSlot(date("2022-10-19T00:00:00"), date("2022-10-19T00:30:00"))),
                ofMinutes(120),
                date("2022-10-19T00:00:00"),
                date("2022-10-19T02:00:00")
        );

        // then
        assertThat(freeTimeSlot).isEmpty();
    }

    @Test
    void should_return_free_time_slot_when_list_has_more_than_one_element() {
        // when
        final var freeTimeSlot = freeTimeSlot(
                List.of(
                        new TimeSlot(date("2022-10-19T00:00:00"), date("2022-10-19T00:30:00")),
                        new TimeSlot(date("2022-10-19T00:00:00"), date("2022-10-19T01:30:00")),
                        new TimeSlot(date("2022-10-19T01:00:00"), date("2022-10-19T02:30:00")),
                        new TimeSlot(date("2022-10-19T03:30:00"), date("2022-10-19T05:30:00")),
                        new TimeSlot(date("2022-10-19T07:30:00"), date("2022-10-19T09:30:00"))
                ),
                ofMinutes(90),
                date("2022-10-19T00:00:00"),
                date("2022-10-19T12:00:00")
        );

        // then
        assertThat(freeTimeSlot).contains(
                new TimeSlot(date("2022-10-19T05:30:00"), date("2022-10-19T07:00:00"))
        );
    }

    @Test
    void should_return_nothing_when_list_has_more_than_one_element_but_does_not_have_free_slots_with_min_duration() {
        // when
        final var freeTimeSlot = freeTimeSlot(
                List.of(
                        new TimeSlot(date("2022-10-19T00:00:00"), date("2022-10-19T00:30:00")),
                        new TimeSlot(date("2022-10-19T00:00:00"), date("2022-10-19T01:30:00")),
                        new TimeSlot(date("2022-10-19T01:00:00"), date("2022-10-19T02:30:00")),
                        new TimeSlot(date("2022-10-19T03:30:00"), date("2022-10-19T05:30:00")),
                        new TimeSlot(date("2022-10-19T06:30:00"), date("2022-10-19T09:00:00"))
                ),
                ofMinutes(120),
                date("2022-10-19T00:00:00"),
                date("2022-10-19T10:00:00")
        );

        // then
        assertThat(freeTimeSlot).isEmpty();
    }

    private static LocalDateTime date(String string) {
        return parse(string);
    }
}