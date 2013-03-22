package org.cujau.db2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cujau.db2.dto.TestInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IDBSchemaListenerTest {

    private static final Logger LOG = LoggerFactory.getLogger( IDBSchemaListenerTest.class );
    private TestDBUtility dbUtil;

    @Before
    public void before()
            throws IOException {
        dbUtil = new TestDBUtility();
        dbUtil.setDialect( TestDBUtility.getDefaultDialect() );
        dbUtil.setDataSource( TestDBUtility.getDefaultDataSource() );
    }

    @After
    public void after() {
    // Nothing to do.
    }

    @Test
    public void testGetVersionWithSchemaListener() {
        SchemaListener listener = new SchemaListener();
        dbUtil.addSchemaListener( listener );

        dbUtil.createDBSchema();
        int version = dbUtil.getDBVersion();
        assertTrue( version == 0 );
        dbUtil.dropDBSchema();

        assertTrue( listener.preCreate == 1 );
        assertTrue( listener.postCreate == 1 );
        assertTrue( listener.preMigrateUp == 0 );
        assertTrue( listener.migratedUps.isEmpty() );
        assertTrue( listener.postMigrateUp == 0 );
        assertTrue( listener.preMigrateDown == 0 );
        assertTrue( listener.migratedDowns.isEmpty() );
        assertTrue( listener.postMigrateDown == 0 );
        assertTrue( listener.preDrop == 1 );
        assertTrue( listener.postDrop == 1 );
    }

    @Test
    public void testGetVersionWithSchemaListenerWithVeto() {
        SchemaListener listener = new SchemaListener();
        listener.preReturn = false;
        dbUtil.addSchemaListener( listener );

        dbUtil.createDBSchema();
        boolean exception = false;
        try {
            dbUtil.getDBVersion();
        } catch ( Exception e ) {
            exception = true;
            LOG.info( "Testing exception got exception. Good! : {}", e.getMessage() );
        }
        if ( !exception ) {
            fail( "Should not have been able to getDBVersion without exception! DB should not have been created." );
        }
        dbUtil.dropDBSchema();

        assertTrue( listener.preCreate == 1 );
        assertTrue( listener.postCreate == 0 );
        assertTrue( listener.preMigrateUp == 0 );
        assertTrue( listener.migratedUps.isEmpty() );
        assertTrue( listener.postMigrateUp == 0 );
        assertTrue( listener.preMigrateDown == 0 );
        assertTrue( listener.migratedUps.isEmpty() );
        assertTrue( listener.postMigrateDown == 0 );
        assertTrue( listener.preDrop == 1 );
        assertTrue( listener.postDrop == 0 );
    }

    @Test
    public void testMigrateUpDownWithSchemaListener()
            throws MigrationInitializationException {
        SchemaListener listener = new SchemaListener();
        dbUtil.addSchemaListener( listener );

        dbUtil.createDBSchema();
        int version = dbUtil.getDBVersion();
        assertTrue( version == 0 );
        List<TestInfo> tis = dbUtil.getTestInfoDAO().selectAll();
        assertTrue( "expected size=3, but got " + tis.size(), tis.size() == 3 );

        dbUtil.migrateDBSchemaUp();
        version = dbUtil.getDBVersion();
        assertTrue( version == 2 );
        assertEquals( 2, listener.migratedUps.size() );
        // Migration 1 removes a row, so recheck the count too.
        tis = dbUtil.getTestInfoDAO().selectAll();
        assertTrue( "expected size=2, but got " + tis.size(), tis.size() == 2 );

        dbUtil.getTestInfoDAO().insert( "four", 4 );
        dbUtil.getTestInfoDAO().insertPostMigrate( "five", 5, "HIGH" );

        tis = dbUtil.getTestInfoDAO().selectAll();
        assertTrue( "expected size=4, but got " + tis.size(), tis.size() == 4 );
        assertNull( tis.get( 0 ).getPrio() );
        assertNull( tis.get( 1 ).getPrio() );
        assertNull( tis.get( 2 ).getPrio() );
        assertNotNull( tis.get( 3 ).getPrio() );
        assertTrue( tis.get( 3 ).getPrio().equals( "HIGH" ) );

        // The second migration should not do anything.
        dbUtil.migrateDBSchemaUp();
        version = dbUtil.getDBVersion();
        assertTrue( version == 2 );
        assertTrue( listener.migratedUps.size() == 2 );
        tis = dbUtil.getTestInfoDAO().selectAll();
        assertTrue( "expected size=4, but got " + tis.size(), tis.size() == 4 );

        dbUtil.migrateDBSchemaDown();
        version = dbUtil.getDBVersion();
        assertTrue( version == 0 );
        assertTrue( listener.migratedDowns.size() == 2 );
        // Migration 1 will add a row, so recheck the count.
        tis = dbUtil.getTestInfoDAO().selectAll();
        assertTrue( "expected size=5, but got " + tis.size(), tis.size() == 5 );

        // The second migration down should not do anything.
        dbUtil.migrateDBSchemaDown();
        version = dbUtil.getDBVersion();
        assertTrue( version == 0 );
        assertTrue( listener.migratedDowns.size() == 2 );
        tis = dbUtil.getTestInfoDAO().selectAll();
        assertTrue( "expected size=5, but got " + tis.size(), tis.size() == 5 );

        dbUtil.dropDBSchema();

        assertTrue( listener.preCreate == 1 );
        assertTrue( listener.postCreate == 1 );
        assertTrue( listener.preMigrateUp == 1 );
        assertTrue( listener.postMigrateUp == 1 );
        assertTrue( listener.preMigrateDown == 1 );
        assertTrue( listener.postMigrateDown == 1 );
        assertTrue( listener.preDrop == 1 );
        assertTrue( listener.postDrop == 1 );
    }

    private class SchemaListener implements IDBSchemaListener {

        public boolean preReturn = true;
        public int preCreate;
        public int preDrop;
        public int postCreate;
        public int postDrop;
        public int preMigrateUp;
        public List<Integer> migratedUps = new ArrayList<Integer>();
        public int postMigrateUp;
        public int preMigrateDown;
        public List<Integer> migratedDowns = new ArrayList<Integer>();
        public int postMigrateDown;

        @Override
        public boolean preSchemaCreate( AbstractDBUtility dbutil ) {
            preCreate += 1;
            return preReturn;
        }

        @Override
        public void postSchemaCreate( AbstractDBUtility dbutil ) {
            postCreate += 1;
            dbUtil.getTestInfoDAO().insert( "one", 1 );
            dbUtil.getTestInfoDAO().insert( "two", 2 );
            dbUtil.getTestInfoDAO().insert( "three", 3 );
        }

        @Override
        public boolean preSchemaDrop( AbstractDBUtility dbutil ) {
            preDrop += 1;
            return preReturn;
        }

        @Override
        public void postSchemaDrop( AbstractDBUtility dbutil ) {
            postDrop += 1;
        }

        @Override
        public boolean preSchemaMigrateUp( AbstractDBUtility dbutil ) {
            preMigrateUp += 1;
            return preReturn;
        }

        @Override
        public void postSchemaMigrateUp( int migrationNumber, AbstractDBUtility dbutil ) {
            migratedUps.add( migrationNumber );
        }
        
        @Override
        public void postSchemaMigrateUp( AbstractDBUtility dbutil ) {
            postMigrateUp += 1;
        }

        @Override
        public boolean preSchemaMigrateDown( AbstractDBUtility dbutil ) {
            preMigrateDown += 1;
            return preReturn;
        }

        @Override
        public void postSchemaMigrateDown( int migrationNumber, AbstractDBUtility dbutil ) {
            migratedDowns.add( migrationNumber );
        }

        @Override
        public void postSchemaMigrateDown( AbstractDBUtility dbutil ) {
            postMigrateDown += 1;
        }


    }

}
