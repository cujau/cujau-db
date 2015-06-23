package org.cujau.db2.jdbc;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cujau.db2.H2DBUtilityHelpers;
import org.cujau.db2.SimpleTestDBUtility;
import org.cujau.db2.dto.SimpleTestDTO;
import org.cujau.utils.TimingUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Performance test to show that our mega-if-else statement is more performant than the more elegant
 * enum solution.
 */
public class JDBCUtilsTest {

    SimpleTestDBUtility dbutil;

    @Before
    public void before()
            throws IOException {
        dbutil = new SimpleTestDBUtility();
        H2DBUtilityHelpers.initAndCreateInMemoryDB( dbutil );
    }

    @After
    public void after() {
        dbutil.dropDBSchema();
    }

    @Test
    public void testPerformance() {
        Connection connection = null;
        PreparedStatement stmt = null;

        connection = JDBCUtils.getConnection( dbutil.getDataSource() );

        SimpleTestDTO dtoObj = new SimpleTestDTO();
        dtoObj.setId( 12345 );
        dtoObj.setCash( BigDecimal.valueOf( 1.11156 ) );
        dtoObj.setName( "BlahBlahBlah" );
        dtoObj.setSymbol( "BBB" );
        dtoObj.setUseful( false );
        Map<String, Object> params = new HashMap<String, Object>();
        params.put( "id", dtoObj.getId() );
        dbutil.getSimpleTestDAO().fillParams( dtoObj, params );

        List<String> columnNames = dbutil.getSimpleTestDAO().getColumnNames();

        // Use this to try out specific types.
        // params = new HashMap<String, Object>();
        // params.put( "id", 12.123f );
        // params.put( "cash", 12.123f );
        // params.put( "name", 12.123f );
        // params.put( "symbol", 12.123f );
        // params.put( "is_useful", 12.123f );

        // Set up an initial statement.
        stmt =
            JDBCUtils.prepareInsertStatement( connection, dbutil.getSimpleTestDAO().getInsertQuery(), "id",
                                              columnNames, params );

        // Do the test.
        TimingUtil.startTiming( "performance" );
        for ( int i = 0; i < 1000000; i++ ) {
            JDBCUtils.fillPreparedStatement( stmt, columnNames, params );
        }
        TimingUtil.stopTiming( "performance" );

        JDBCUtils.cleanup( connection, stmt, null );

    }
}
