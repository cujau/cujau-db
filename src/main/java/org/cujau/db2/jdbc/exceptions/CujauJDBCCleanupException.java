package org.cujau.db2.jdbc.exceptions;

import java.sql.SQLException;

public class CujauJDBCCleanupException extends CujauJDBCException {

    private static final long serialVersionUID = -7104994030526068656L;

    public CujauJDBCCleanupException(SQLException e) {
        super(e);
    }
}
