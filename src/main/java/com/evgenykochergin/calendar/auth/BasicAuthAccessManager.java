package com.evgenykochergin.calendar.auth;

import com.evgenykochergin.calendar.service.UserService;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.security.AccessManager;
import io.javalin.security.RouteRole;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static io.javalin.http.HttpStatus.UNAUTHORIZED;
import static java.util.Objects.requireNonNull;

public class BasicAuthAccessManager implements AccessManager {

    private final static String PRINCIPAL = "principal";

    private final UserService userService;

    public BasicAuthAccessManager(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void manage(@NotNull Handler handler, @NotNull Context context, @NotNull Set<? extends RouteRole> set) throws Exception {
        if (set.isEmpty()) {
            handler.handle(context);
            return;
        }
        final var credentials = context.basicAuthCredentials();
        if (credentials != null) {
            final var user = userService.findByUsername(credentials.getUsername());
            if (user.isPresent() && user.get().password.equals(credentials.getPassword())) {
                context.attribute(PRINCIPAL, new Principal(user.get().id, user.get().username));
                handler.handle(context);
                return;
            }
        }
        context.status(UNAUTHORIZED).json("Unauthorized");
    }

    public static Principal principal(Context ctx) {
        return requireNonNull(ctx.attribute(PRINCIPAL));
    }
}
