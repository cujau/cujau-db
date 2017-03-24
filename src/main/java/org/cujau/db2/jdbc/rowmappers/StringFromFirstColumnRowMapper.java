package org.cujau.db2.jdbc.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.cujau.db2.jdbc.TypedRowMapper;

public class StringFromFirstColumnRowMapper implements TypedRowMapper<String> {

    @Override
    public String mapRow(ResultSet rs, int rowNumber)
            throws SQLException {
        return rs.getString(1);
    }

}
