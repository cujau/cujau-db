package org.cujau.db2.jdbc.exceptions;

import java.sql.SQLException;

public class CujauJDBCExecutionException extends CujauJDBCException {

    private static final long serialVersionUID = -6224789708301704035L;

    public CujauJDBCExecutionException(SQLException e) {
        super(e);
    }
}
