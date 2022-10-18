package com.evgenykochergin.calendar.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

public class Json {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.findAndRegisterModules();
        OBJECT_MAPPER.registerModule(new Jdk8Module());
    }

    public static ObjectNode objectNode() {
        return OBJECT_MAPPER.createObjectNode();
    }

    public static ArrayNode arrayNode() {
        return OBJECT_MAPPER.createArrayNode();
    }
}
