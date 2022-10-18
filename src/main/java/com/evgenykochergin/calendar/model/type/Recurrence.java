package com.evgenykochergin.calendar.model.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class Recurrence {
    public final RecurrenceFrequency frequency;
    public final LocalDateTime endDate;

    @JsonCreator
    public Recurrence(@JsonProperty("frequency") RecurrenceFrequency frequency,
                      @JsonProperty("endDate") LocalDateTime endDate) {
        this.frequency = frequency;
        this.endDate = endDate;
    }
}
