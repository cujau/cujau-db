package org.cujau.db2.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetRowHandler {

    void handleRow(ResultSet rs, int rowNumber)
            throws SQLException;

}
