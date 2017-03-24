package org.cujau.db2.jdbc.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.cujau.db2.jdbc.TypedRowMapper;

public class IntegerFromFirstColumnRowMapper implements TypedRowMapper<Integer> {

    @Override
    public Integer mapRow(ResultSet rs, int rowNumber)
            throws SQLException {
        return rs.getInt(1);
    }

}
