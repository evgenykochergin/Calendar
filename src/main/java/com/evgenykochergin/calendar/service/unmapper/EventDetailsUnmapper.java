package com.evgenykochergin.calendar.service.unmapper;

import com.evgenykochergin.calendar.db.tables.records.EventDetailsRecord;
import com.evgenykochergin.calendar.model.EventDetails;
import org.jetbrains.annotations.NotNull;
import org.jooq.RecordUnmapper;
import org.jooq.exception.MappingException;

public class EventDetailsUnmapper implements RecordUnmapper<EventDetails, EventDetailsRecord> {

    @Override
    public @NotNull EventDetailsRecord unmap(EventDetails eventDetails) throws MappingException {
        return new EventDetailsRecord()
                .setId(eventDetails.id)
                .setOrganizerId(eventDetails.organizerId)
                .setName(eventDetails.name)
                .setVisibility(eventDetails.visibility.name())
                .setDescription(eventDetails.description.orElse(null));
    }
}
