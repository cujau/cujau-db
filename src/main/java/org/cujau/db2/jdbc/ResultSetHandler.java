package org.cujau.db2.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetHandler {

    void handleResultSet( ResultSet rs ) throws SQLException;
    
}
