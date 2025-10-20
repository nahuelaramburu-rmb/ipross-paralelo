package com.capacidad.identityservice.functional;

import java.util.function.Supplier;

@FunctionalInterface
public interface ThrowingSupplier<T, E extends Exception> {
    static <T> Supplier<T> throwingSupplier(
            ThrowingSupplier<T, Exception> throwingSupplier) {

        return () -> {
            try {
                return throwingSupplier.get();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    T get() throws E;
}