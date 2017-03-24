package org.cujau.db2.jdbc.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.cujau.db2.jdbc.TypedRowMapper;

public class FloatFromFirstColumnRowMapper implements TypedRowMapper<Float> {

    @Override
    public Float mapRow(ResultSet rs, int rowNumber)
            throws SQLException {
        return rs.getFloat(1);
    }

}
