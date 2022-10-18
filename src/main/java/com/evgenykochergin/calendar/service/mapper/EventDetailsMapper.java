package com.evgenykochergin.calendar.service.mapper;

import com.evgenykochergin.calendar.db.tables.records.EventDetailsRecord;
import com.evgenykochergin.calendar.model.EventDetails;
import com.evgenykochergin.calendar.model.type.EventDetailsVisibility;
import org.jetbrains.annotations.Nullable;
import org.jooq.RecordMapper;

import static com.evgenykochergin.calendar.model.EventDetails.eventDetails;
import static java.util.Optional.ofNullable;

public class EventDetailsMapper implements RecordMapper< EventDetailsRecord,EventDetails> {

    @Override
    public @Nullable EventDetails map(EventDetailsRecord eventDetailsRecord) {
        return eventDetails()
                .id(eventDetailsRecord.getId())
                .organizerId(eventDetailsRecord.getOrganizerId())
                .name(eventDetailsRecord.getName())
                .visibility(EventDetailsVisibility.valueOf(eventDetailsRecord.getVisibility()))
                .description(ofNullable(eventDetailsRecord.getDescription()))
                .build();
    }
}
