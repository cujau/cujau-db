package org.cujau.db2.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface TypedRowMapper<T> {

    T mapRow(ResultSet rs, int rowNumber)
            throws SQLException;

}
