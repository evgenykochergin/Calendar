package com.evgenykochergin.calendar.model;

import org.junit.jupiter.api.Test;

import static com.evgenykochergin.calendar.model.EventDetails.eventDetails;
import static com.evgenykochergin.calendar.model.type.EventDetailsVisibility.PUBLIC;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

class EventDetailsTest {

    @Test
    void should_create_event_details() {
        // given
        final var name = "name";
        final var description = "description";
        final var organizerId = randomUUID();
        final var visibility = PUBLIC;

        // when
        final var eventDetails = eventDetails()
                .name(name)
                .description(description)
                .organizerId(organizerId)
                .visibility(visibility)
                .build();

        // then
        assertThat(eventDetails.name).isEqualTo(name);
        assertThat(eventDetails.description).contains(description);
        assertThat(eventDetails.organizerId).isEqualTo(organizerId);
        assertThat(eventDetails.visibility).isEqualTo(visibility);
    }
}