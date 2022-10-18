package com.evgenykochergin.calendar.service.unmapper;

import com.evgenykochergin.calendar.db.tables.records.EventRecord;
import com.evgenykochergin.calendar.model.Event;
import org.jetbrains.annotations.NotNull;
import org.jooq.RecordUnmapper;
import org.jooq.exception.MappingException;

public class EventUnmapper implements RecordUnmapper<Event, EventRecord> {

    @Override
    public @NotNull EventRecord unmap(Event event) throws MappingException {
        return new EventRecord()
                .setId(event.id)
                .setUserId(event.userId)
                .setEventDetailsId(event.eventDetailsId)
                .setStartDate(event.startDate)
                .setEndDate(event.endDate)
                .setDuration(event.duration.toMinutes())
                .setType(event.type.name())
                .setStatus(event.status.name())
                .setRecurrenceFreq(event.recurrence.map(recurrence -> recurrence.frequency.name()).orElse(null));
    }
}
