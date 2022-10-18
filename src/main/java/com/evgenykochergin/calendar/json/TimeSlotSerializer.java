package com.evgenykochergin.calendar.json;

import com.evgenykochergin.calendar.service.FreeTimeSlotFinder.TimeSlot;
import com.fasterxml.jackson.databind.JsonNode;

import static com.evgenykochergin.calendar.json.Json.objectNode;

public class TimeSlotSerializer {

    private TimeSlotSerializer() {
    }

    public static JsonNode timeSlotJson(TimeSlot timeSlot) {
        return objectNode()
                .put("startDate", timeSlot.startDate().toString())
                .put("endDate", timeSlot.endDate().toString());
    }
}
