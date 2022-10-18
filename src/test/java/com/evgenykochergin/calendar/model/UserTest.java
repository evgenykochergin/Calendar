package com.evgenykochergin.calendar.model;

import org.junit.jupiter.api.Test;

import static com.evgenykochergin.calendar.model.User.user;
import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void should_create_user() {
        // given
        final var username = "username";
        final var password = "password";

        // then
        final var user = user()
                .username(username)
                .password(password)
                .build();

        // when
        assertThat(user.username).isEqualTo(username);
        assertThat(user.password).isEqualTo(password);
    }
}