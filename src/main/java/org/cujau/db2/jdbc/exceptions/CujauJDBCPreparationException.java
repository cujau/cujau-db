package org.cujau.db2.jdbc.exceptions;

import java.sql.SQLException;

public class CujauJDBCPreparationException extends CujauJDBCException {
    
    private static final long serialVersionUID = 6240156867373112296L;

    public CujauJDBCPreparationException( SQLException e ) {
        super( e );
    }
}
