package com.evgenykochergin.calendar;

import com.evgenykochergin.calendar.db.tables.Event;
import com.evgenykochergin.calendar.service.EventService;
import com.evgenykochergin.calendar.service.UserService;
import io.restassured.RestAssured;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.net.ServerSocket;

import static com.evgenykochergin.calendar.db.tables.EventDetails.EVENT_DETAILS;
import static com.evgenykochergin.calendar.db.tables.User.USER;

public class FunctionalTest {

    private static final Application app = new Application();
    protected final UserService userService = app.userService;
    protected final EventService eventService = app.eventService;

    @BeforeAll
    static void beforeAll() {
        final var port = findFreePort();
        app.start(port);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @AfterEach
    void tearDown() {
        cleanup(app.db);
    }

    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0);) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("Could not find free TCP/IP port");
        }
    }

    private static void cleanup(DSLContext db) {
        db.deleteFrom(Event.EVENT).execute();
        db.deleteFrom(EVENT_DETAILS).execute();
        db.deleteFrom(USER).execute();
    }
}