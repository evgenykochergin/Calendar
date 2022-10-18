package com.evgenykochergin.calendar.json;


import com.evgenykochergin.calendar.model.Event;
import com.evgenykochergin.calendar.model.EventDetails;
import com.evgenykochergin.calendar.model.type.Recurrence;
import com.evgenykochergin.calendar.model.type.UserEventStatus;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Collection;

import static com.evgenykochergin.calendar.json.Json.arrayNode;
import static com.evgenykochergin.calendar.json.Json.objectNode;

public class EventSerializer {

    private EventSerializer() {
    }

    public static ObjectNode eventJson(Event event) {
        final var objectNode = objectNode()
                .put("id", event.id.toString())
                .put("startDate", event.startDate.toString())
                .put("endDate", event.startDate.plus(event.duration).toString())
                .put("duration", event.duration.toMinutes())
                .put("type", event.type.name());
        event.recurrence.map(EventSerializer::recurrenceJson).ifPresent(jsonNode -> objectNode.set("recurrence", jsonNode));
        return objectNode;
    }

    public static ObjectNode eventWithDetailsJson(Event event,
                                                  EventDetails eventDetails,
                                                  Collection<UserEventStatus> userEventStatuses) {
        return eventJson(event).set("details", eventDetailsJson(eventDetails, userEventStatuses));
    }

    private static ObjectNode eventDetailsJson(EventDetails eventDetails,
                                               Collection<UserEventStatus> userEventStatuses) {
        final var objectNode = objectNode()
                .put("name", eventDetails.name)
                .put("organizerId", eventDetails.organizerId.toString())
                .put("visibility", eventDetails.visibility.name());
        eventDetails.description.ifPresent(description -> objectNode.put("description", description));
        return objectNode.set("attendees", arrayNode().addAll(userEventStatuses.stream().map(EventSerializer::userJson).toList()));
    }

    private static ObjectNode userJson(UserEventStatus userEventStatus) {
        return objectNode()
                .put("userId", userEventStatus.userId.toString())
                .put("status", userEventStatus.status.name());
    }

    private static ObjectNode recurrenceJson(Recurrence recurrence) {
        return objectNode()
                .put("frequency", recurrence.frequency.name())
                .put("endDate", recurrence.endDate.toString());
    }
}