package com.evgenykochergin.calendar.model.type;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static com.evgenykochergin.calendar.model.type.RecurrenceFrequency.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

class RecurrenceFrequencyTest {

    @ParameterizedTest
    @MethodSource
    void should_create_next_date(RecurrenceFrequency frequency, LocalDateTime date, LocalDateTime nextDate) {
        // then
        assertThat(frequency.nextDate(date)).isEqualTo(nextDate);
    }

    private static Stream<Arguments> should_create_next_date() {
        return Stream.of(
                of(DAILY, LocalDateTime.parse("2022-10-18T05:00"), LocalDateTime.parse("2022-10-19T05:00")),
                of(WEEKLY, LocalDateTime.parse("2022-10-18T05:00"), LocalDateTime.parse("2022-10-25T05:00")),
                of(MONTHLY, LocalDateTime.parse("2022-10-18T05:00"), LocalDateTime.parse("2022-11-18T05:00")),
                of(ANNUALLY, LocalDateTime.parse("2022-10-18T05:00"), LocalDateTime.parse("2023-10-18T05:00")),
                of(EVERY_WEEKDAY, LocalDateTime.parse("2022-10-18T05:00"), LocalDateTime.parse("2022-10-19T05:00")),
                of(EVERY_WEEKDAY, LocalDateTime.parse("2022-10-21T05:00"), LocalDateTime.parse("2022-10-24T05:00")),
                of(EVERY_WEEKDAY, LocalDateTime.parse("2022-10-22T05:00"), LocalDateTime.parse("2022-10-24T05:00")),
                of(EVERY_WEEKDAY, LocalDateTime.parse("2022-10-23T05:00"), LocalDateTime.parse("2022-10-24T05:00")),
                of(EVERY_WEEKDAY, LocalDateTime.parse("2022-10-24T05:00"), LocalDateTime.parse("2022-10-25T05:00"))
        );
    }
}