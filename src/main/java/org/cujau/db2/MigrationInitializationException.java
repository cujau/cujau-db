package org.cujau.db2;

public class MigrationInitializationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MigrationInitializationException(Throwable t) {
        super(t);
    }

    public MigrationInitializationException(String message) {
        super(message);
    }

    public MigrationInitializationException(String message, Throwable t) {
        super(message, t);
    }

}
