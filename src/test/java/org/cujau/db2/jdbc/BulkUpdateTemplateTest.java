package org.cujau.db2.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.cujau.db2.DBUtilityTestHelpers;
import org.cujau.db2.SimpleTestDBUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BulkUpdateTemplateTest {

    SimpleTestDBUtility dbutil;

    @Before
    public void before()
            throws IOException {
        dbutil = new SimpleTestDBUtility();
        DBUtilityTestHelpers.initAndCreateInMemoryDB( dbutil );
    }

    @After
    public void after() {
        dbutil.dropDBSchema();
    }

    @Test
    public void testShouldWork() {
        BulkUpdateTemplate bulk =
            new BulkUpdateTemplate( dbutil.getDataSource(),
                                    "insert into simple_test( name, is_useful, symbol ) values( ?, ?, ? )" );
        bulk.begin();
        bulk.update( "nick", true, "NPR" );
        bulk.update( "beth", true, "BAR" );
        bulk.end();

        assertEquals( 2, dbutil.getSimpleTestDAO().selectCount() );

        bulk.begin();
        bulk.update( "fred", false, "FST" );
        bulk.update( "barny", true, "BNY" );
        bulk.endWithRollback();

        assertEquals( 2, dbutil.getSimpleTestDAO().selectCount() );
    }

    @Test
    // ( expected = CujauJDBCExecutionException.class )
    public void testWontWork() {
        boolean exceptionThrown = false;
        BulkUpdateTemplate bulk =
            new BulkUpdateTemplate( dbutil.getDataSource(),
                                    "insert into simple_test( name, is_useful, symbol ) values( ?, ?, ? )" );
        bulk.begin();
        bulk.update( "nick", true, "NPR" );
        bulk.update( "beth", true, "BAR" );

        try {
            // This will throw a CujauJDBCExecutionException because it is trying to lock the table
            // we are inserting into.
            assertEquals( 0, dbutil.getSimpleTestDAO().selectCount() );

            // The same would happen if we opened a new bulk template to the same table with the
            // same connection.
            //
            // BulkUpdateTemplate bulk2 =
            // new BulkUpdateTemplate( dbutil.getDataSource(),
            // "insert into simple_test( name, is_useful, symbol ) values( ?, ?, ? )" );
            // bulk2.begin();
            // bulk2.update( "fred", false, "FST" );
        } catch ( Exception e ) {
            exceptionThrown = true;
        } finally {
            // Make sure we close this or we get an exception in after() because the table is still
            // locked.
            bulk.end();
        }

        // Updates committed correctly.
        assertEquals( 2, dbutil.getSimpleTestDAO().selectCount() );
        // And the exception was thrown.
        assertTrue( exceptionThrown );
        exceptionThrown = false;

        bulk.begin();
        bulk.update( "bill", true, "BIL" );
        bulk.update( "fred", true, "FRD" );

        try {
            // This will throw a CujauJDBCExecutionException because it is trying to lock the table
            // we are inserting into.
            assertEquals( 0, dbutil.getSimpleTestDAO().selectCount() );
        } catch ( Exception e ) {
            exceptionThrown = true;
        } finally {
            bulk.endWithRollback();
        }

        // Inserts not committed (would e 4 otherwise).
        assertEquals( 2, dbutil.getSimpleTestDAO().selectCount() );
        // And an exception was thrown.
        assertTrue( exceptionThrown );
    }
}
