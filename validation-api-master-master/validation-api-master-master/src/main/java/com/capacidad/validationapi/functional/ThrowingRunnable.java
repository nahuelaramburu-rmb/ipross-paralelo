package com.capacidad.validationapi.functional;

@FunctionalInterface
public interface ThrowingRunnable<E extends Exception> {
    static Runnable throwingRunnable(
            ThrowingRunnable<Exception> throwingRunnable) {

        return () -> {
            try {
                throwingRunnable.run();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }


    void run() throws E;


}
