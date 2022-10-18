package com.evgenykochergin.calendar.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.time.Duration.between;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;

public class FreeTimeSlotFinder {

    public record TimeSlot(LocalDateTime startDate, LocalDateTime endDate) {
        public TimeSlot(LocalDateTime startDate, LocalDateTime endDate) {
            this.startDate = requireNonNull(startDate);
            this.endDate = requireNonNull(endDate);
            if (!startDate.isBefore(endDate)) {
                throw new IllegalArgumentException("startDate should be before endDate");
            }
        }
    }

    public static Optional<TimeSlot> freeTimeSlot(List<TimeSlot> sourceBusyTimeSlots,
                                                  Duration duration,
                                                  LocalDateTime fromDate,
                                                  LocalDateTime toDate) {

        final var busyTimeSlots = sourceBusyTimeSlots.stream()
                .sorted(comparing((Function<TimeSlot, LocalDateTime>) busyWindow -> busyWindow.startDate)
                        .thenComparing(busyWindow -> busyWindow.endDate))
                .toList();
        if (busyTimeSlots.isEmpty()) {
            final var endDate = fromDate.plus(duration);
            if (endDate.compareTo(toDate) <= 0) {
                return Optional.of(new TimeSlot(fromDate, endDate));
            }
            return empty();
        }
        var busyTimeSlot = busyTimeSlots.get(0);
        if (fromDate.isBefore(busyTimeSlot.startDate)) {
            if (between(fromDate, busyTimeSlot.startDate).compareTo(duration) >= 0) {
                if (busyTimeSlot.startDate.compareTo(toDate) <= 0) {
                    return Optional.of(new TimeSlot(fromDate, busyTimeSlot.startDate));
                }
                return empty();
            }
        }
        int index = 1;
        while (index < busyTimeSlots.size()) {
            final var nextBusySlot = busyTimeSlots.get(index);
            if (busyTimeSlot.endDate.isBefore(nextBusySlot.startDate)) {
                if (between(busyTimeSlot.endDate, nextBusySlot.startDate).compareTo(duration) >= 0) {
                    if (busyTimeSlot.endDate.plus(duration).compareTo(toDate) <= 0) {
                        return Optional.of(new TimeSlot(busyTimeSlot.endDate, busyTimeSlot.endDate.plus(duration)));
                    }
                }
                busyTimeSlot = nextBusySlot;
            } else {
                busyTimeSlot = new TimeSlot(busyTimeSlot.startDate, nextBusySlot.endDate);
            }
            index++;
        }
        if (busyTimeSlot.endDate.plus(duration).compareTo(toDate) <= 0) {
            return Optional.of(new TimeSlot(busyTimeSlot.endDate, busyTimeSlot.endDate.plus(duration)));
        }
        return empty();
    }
}
