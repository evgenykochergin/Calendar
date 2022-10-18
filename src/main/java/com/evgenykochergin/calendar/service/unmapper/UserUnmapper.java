package com.evgenykochergin.calendar.service.unmapper;

import com.evgenykochergin.calendar.db.tables.records.UserRecord;
import com.evgenykochergin.calendar.model.User;
import org.jetbrains.annotations.NotNull;
import org.jooq.RecordUnmapper;
import org.jooq.exception.MappingException;

public class UserUnmapper implements RecordUnmapper<User, UserRecord> {

    @Override
    public @NotNull UserRecord unmap(User user) throws MappingException {
        return new UserRecord()
                .setId(user.id)
                .setUsername(user.username)
                .setPassword(user.password);
    }
}
