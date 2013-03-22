package org.cujau.db2.jdbc.exceptions;

import java.sql.SQLException;

public class CujauJDBCException extends RuntimeException {
    
    private static final long serialVersionUID = -7948503729377648468L;

    public CujauJDBCException() {
        super();
    }

    public CujauJDBCException( SQLException e ) {
        super( e );
    }
}
