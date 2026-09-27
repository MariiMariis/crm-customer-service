package com.pb.crm.commons.actor;

import java.util.Optional;
import java.util.function.Supplier;

public final class MessageContext {

    private static final ThreadLocal<Current> CURRENT = new ThreadLocal<>();

    private MessageContext() {
    }

    public record Current(String actor, String correlationId, String causationId) {
    }

    public static Optional<Current> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static <T> T callWith(Current context, Supplier<T> action) {
        Current previous = CURRENT.get();
        CURRENT.set(context);
        try {
            return action.get();
        } finally {
            if (previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(previous);
            }
        }
    }

    public static void runWith(Current context, Runnable action) {
        callWith(context, () -> {
            action.run();
            return null;
        });
    }
}
