package com.capacidad.identityservice.functional;

@FunctionalInterface
public interface ThrowingRunnable<E extends Exception> {
    static Runnable throwingRunnable(
            ThrowingRunnable<Exception> throwingConsumer) {

        return () -> {
            try {
                throwingConsumer.run();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    void run() throws E;
}
