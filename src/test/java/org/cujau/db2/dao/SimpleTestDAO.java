package org.cujau.db2.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.cujau.db2.AbstractDBUtility;
import org.cujau.db2.DAOInitializationException;
import org.cujau.db2.dto.SimpleTestDTO;
import org.cujau.db2.jdbc.TypedRowMapper;

public class SimpleTestDAO extends AbstractInsertUpdateDAO<SimpleTestDTO> {

    private static final String TABLE_NAME = "simple_test";

    public SimpleTestDAO( Properties props, AbstractDBUtility dbutil )
            throws DAOInitializationException {
        super( props, dbutil );
    }

    @Override
    protected String getBaseTableName() {
        return TABLE_NAME;
    }

    @Override
    protected TypedRowMapper<SimpleTestDTO> createRowMapper() {
        return new SimpleTestRowMapper();
    }

    @Override
    public Map<String, Object> fillParams( SimpleTestDTO dtoObj, Map<String, Object> params ) {
        params.put( "is_useful", dtoObj.isUseful() );
        params.put( "name", dtoObj.getName() );
        params.put( "symbol", dtoObj.getSymbol() );
        params.put( "cash", dtoObj.getCash() );
        return params;
    }

    protected class SimpleTestRowMapper implements TypedRowMapper<SimpleTestDTO> {
        @Override
        public SimpleTestDTO mapRow( ResultSet rs, int rowNum )
                throws SQLException {
            SimpleTestDTO row = new SimpleTestDTO();
            mapIdentityRow( row, rs, rowNum );

            row.setUseful( rs.getBoolean( "is_useful" ) );
            row.setName( rs.getString( "name" ) );
            row.setSymbol( rs.getString( "symbol" ) );
            return row;
        }
    }

    @Override
    public List<String> getColumnNames() {
        List<String> columns = new ArrayList<String>();
        columns.add( "is_useful" );
        columns.add( "name" );
        columns.add( "symbol" );
        columns.add( "cash" );
        return columns;
    }
}
