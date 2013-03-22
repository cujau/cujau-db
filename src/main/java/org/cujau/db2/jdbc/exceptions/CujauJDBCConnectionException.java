package org.cujau.db2.jdbc.exceptions;

import java.sql.SQLException;

public class CujauJDBCConnectionException extends CujauJDBCException {
    
    private static final long serialVersionUID = 5949575917047814188L;

    public CujauJDBCConnectionException( SQLException e ) {
        super( e );
    }
}
