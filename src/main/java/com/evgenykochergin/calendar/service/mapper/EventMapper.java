package com.evgenykochergin.calendar.service.mapper;


import com.evgenykochergin.calendar.db.tables.records.EventRecord;
import com.evgenykochergin.calendar.model.Event;
import com.evgenykochergin.calendar.model.type.EventStatus;
import com.evgenykochergin.calendar.model.type.EventType;
import com.evgenykochergin.calendar.model.type.Recurrence;
import com.evgenykochergin.calendar.model.type.RecurrenceFrequency;
import org.jetbrains.annotations.Nullable;
import org.jooq.RecordMapper;

import static com.evgenykochergin.calendar.model.Event.event;
import static java.time.Duration.ofMinutes;
import static java.util.Optional.ofNullable;

public class EventMapper implements RecordMapper<EventRecord, Event> {

    @Override
    public @Nullable Event map(EventRecord eventRecord) {
        return event()
                .id(eventRecord.getId())
                .userId(eventRecord.getUserId())
                .eventDetailsId(eventRecord.getEventDetailsId())
                .status(EventStatus.valueOf(eventRecord.getStatus()))
                .startDate(eventRecord.getStartDate())
                .endDate(eventRecord.getEndDate())
                .duration(ofMinutes(eventRecord.getDuration()))
                .type(EventType.valueOf(eventRecord.getType()))
                .recurrence(ofNullable(eventRecord.getRecurrenceFreq()).map(freq-> new Recurrence(RecurrenceFrequency.valueOf(freq), eventRecord.getEndDate())))
                .build();
    }
}
