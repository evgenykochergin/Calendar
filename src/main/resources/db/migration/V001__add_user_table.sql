CREATE TABLE user (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(50) NOT NULL
);

CREATE UNIQUE INDEX user_username_idx on user(username);