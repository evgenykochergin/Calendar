package com.evgenykochergin.calendar.json;


import com.evgenykochergin.calendar.model.User;
import com.fasterxml.jackson.databind.JsonNode;

import static com.evgenykochergin.calendar.json.Json.objectNode;

public class UserSerializer {

    private UserSerializer() {
    }

    public static JsonNode userJson(User user) {
        return objectNode()
                .put("id", user.id.toString())
                .put("username", user.username);
    }

}
