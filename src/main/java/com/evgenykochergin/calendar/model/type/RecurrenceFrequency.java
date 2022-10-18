package com.evgenykochergin.calendar.model.type;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Set;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.Period.*;

public enum RecurrenceFrequency {

    DAILY(ofDays(1)),
    WEEKLY(ofWeeks(1)),
    MONTHLY(ofMonths(1)),
    ANNUALLY(ofYears(1)),
    EVERY_WEEKDAY(ofDays(1));

    private final static Set<DayOfWeek> WEEKENDS = Set.of(SUNDAY, SATURDAY);
    private final Period period;

    RecurrenceFrequency(Period period) {
        this.period = period;
    }

    public LocalDateTime nextDate(LocalDateTime date) {
        if (this == EVERY_WEEKDAY) {
            var newDate = date.plus(period);
            while (WEEKENDS.contains(newDate.getDayOfWeek())) {
                newDate = newDate.plus(period);
            }
            return newDate;
        }
        return date.plus(period);
    }
}
