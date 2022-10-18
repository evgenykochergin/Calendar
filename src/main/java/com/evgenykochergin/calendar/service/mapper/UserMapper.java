package com.evgenykochergin.calendar.service.mapper;

import com.evgenykochergin.calendar.db.tables.records.UserRecord;
import com.evgenykochergin.calendar.model.User;
import org.jetbrains.annotations.Nullable;
import org.jooq.RecordMapper;

import static com.evgenykochergin.calendar.model.User.user;

public class UserMapper implements RecordMapper<UserRecord, User> {

    @Override
    public @Nullable User map(UserRecord record) {
        return user()
                .id(record.getId())
                .username(record.getUsername())
                .password(record.getPassword())
                .build();
    }
}
