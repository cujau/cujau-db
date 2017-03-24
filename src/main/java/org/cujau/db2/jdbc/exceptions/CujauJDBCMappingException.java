package org.cujau.db2.jdbc.exceptions;

import java.sql.SQLException;

public class CujauJDBCMappingException extends CujauJDBCException {

    private static final long serialVersionUID = 4422936861666774112L;

    public CujauJDBCMappingException(SQLException e) {
        super(e);
    }
}
