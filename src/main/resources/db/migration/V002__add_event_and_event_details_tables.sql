CREATE TABLE event_details (
    id UUID PRIMARY KEY,
    organizer_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    visibility VARCHAR(50) NOT NULL,
    FOREIGN KEY (organizer_id) REFERENCES user(id)
);

CREATE TABLE event (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    event_details_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    duration BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    recurrence_freq VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (event_details_id) REFERENCES event_details(id)
);

-- todo: add indexes