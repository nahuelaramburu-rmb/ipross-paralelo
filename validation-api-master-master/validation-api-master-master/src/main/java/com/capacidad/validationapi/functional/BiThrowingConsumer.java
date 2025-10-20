package com.capacidad.validationapi.functional;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface BiThrowingConsumer<T, U, E extends Exception> {
    static <T, U> BiConsumer<T, U> biThrowingConsumer(
            BiThrowingConsumer<T, U, Exception> throwingConsumer) {

        return (t, u) -> {
            try {
                throwingConsumer.accept(t, u);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    void accept(T t, U u) throws E;
}