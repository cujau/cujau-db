package org.cujau.db2.dao;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class QueryBuilderTest {

    @Test
    public void testBuildInsertQuery() {
        List<String> columns = new ArrayList<String>();
        columns.add( "id" );
        columns.add( "first_name" );
        columns.add( "last_name" );
        String insertQuery =
            QueryBuilder.buildInsertQuery( "mytable", columns, AbstractInsertUpdateDAO.ID_COLUMN_NAME, false );
        assertEquals( "INSERT INTO mytable(first_name,last_name) VALUES(?,?)", insertQuery );

        insertQuery =
            QueryBuilder.buildInsertQuery( "mytable", columns, AbstractInsertUpdateDAO.ID_COLUMN_NAME, true );
        assertEquals( "INSERT INTO mytable(id,first_name,last_name) VALUES(?,?,?)", insertQuery );
    }

    @Test
    public void testBuildUpdateQuery() {
        List<String> columns = new ArrayList<String>();
        columns.add( "id" );
        columns.add( "first_name" );
        columns.add( "last_name" );
        String updateQuery =
            QueryBuilder.buildUpdateQuery( "mytable", columns, AbstractInsertUpdateDAO.ID_COLUMN_NAME );
        assertEquals( "UPDATE mytable SET first_name=?,last_name=? WHERE id=?", updateQuery );
    }

}
