package org.cujau.db2;

public class DAOInitializationException extends Exception {

    private static final long serialVersionUID = 1L;

    public DAOInitializationException(Throwable t) {
        super(t);
    }

    public DAOInitializationException(String message) {
        super(message);
    }

    public DAOInitializationException(String message, Throwable t) {
        super(message, t);
    }

}
