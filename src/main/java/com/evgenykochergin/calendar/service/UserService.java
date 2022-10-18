package com.evgenykochergin.calendar.service;

import com.evgenykochergin.calendar.error.UserAlreadyExistsException;
import com.evgenykochergin.calendar.error.UserNotFoundException;
import com.evgenykochergin.calendar.model.User;
import com.evgenykochergin.calendar.service.mapper.UserMapper;
import com.evgenykochergin.calendar.service.unmapper.UserUnmapper;
import org.jooq.DSLContext;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.evgenykochergin.calendar.db.tables.User.USER;
import static com.evgenykochergin.calendar.model.User.user;
import static java.util.Objects.requireNonNull;

public class UserService {

    public static class CreateUserParams {

        public final String username;
        public final String password;

        public CreateUserParams(String username,
                                String password) {
            this.username = requireNonNull(username, "username is required");
            this.password = requireNonNull(password, "password is required");
        }
    }

    private final DSLContext db;
    private final UserMapper userMapper;
    private final UserUnmapper userUnmapper;


    public UserService(DSLContext dslContext) {
        this.db = dslContext;
        this.userMapper = new UserMapper();
        this.userUnmapper = new UserUnmapper();
    }

    public Optional<User> findByUsername(String username) {
        return db.selectFrom(USER)
                .where(USER.USERNAME.eq(username))
                .fetchOptional(userMapper);
    }

    public Optional<User> findById(UUID id) {
        return db.selectFrom(USER)
                .where(USER.ID.eq(id))
                .fetchOptional(userMapper);
    }

    public User getById(UUID id) {
        return findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public Set<User> findAllByIds(Set<UUID> userIds) {
        return db.selectFrom(USER)
                .where(USER.ID.in(userIds))
                .fetchSet(userMapper);
    }

    public User createUser(CreateUserParams params) {
        if (findByUsername(params.username).isPresent()) {
            throw new UserAlreadyExistsException();
        }
        final var user = user()
                .username(params.username)
                .password(params.password)
                .build();
        db.transaction(tx -> tx.dsl().executeInsert(userUnmapper.unmap(user)));
        return user;
    }
}