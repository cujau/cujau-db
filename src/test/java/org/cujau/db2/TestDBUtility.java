package org.cujau.db2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.cujau.db2.dao.DAO;
import org.cujau.db2.dao.TestInfoDAO;
import org.cujau.db2.migrations.Test1Migration;
import org.cujau.db2.migrations.Test2Migration;
import org.junit.Before;
import org.junit.Test;

public class TestDBUtility extends AbstractDBUtilityImpl {

    TestInfoDAO testinfoDao;

    public static DataSource getDefaultDataSource() {
        BasicDataSource ds = new BasicDataSource();
        // ds.setDriverClassName( "org.hsqldb.jdbcDriver" );
        // ds.setUrl( "jdbc:hsqldb:mem:example" );

        ds.setDriverClassName( "org.h2.Driver" );
        // The DB_CLOSE_DELAY=-1 meands the contents of an in-memory database will be kept as long
        // as the virtual machine is alive. Otherwise, they are lost when the last connection to the
        // in-memory db is closed.
        ds.setUrl( "jdbc:h2:mem:example;DB_CLOSE_DELAY=-1;TRACE_LEVEL_FILE=4" );
        ds.setUsername( "sa" );
        ds.setPassword( "" );
        return ds;
    }

    public static String getDefaultDialect() {
        return "h2";
        // return "hsqldb";
    }

    @Before
    public void before()
            throws IOException {
        setDialect( "h2" );
        setDataSource( getDefaultDataSource() );
    }

    public void setDBVersion( int version ) {
        getDBVersionDAO().setVersion( version );
    }

    public TestInfoDAO getTestInfoDAO() {
        return testinfoDao;
    }

    @Override
    public List<DAO> instantiateDAOs( Properties props )
            throws DAOInitializationException {
        ArrayList<DAO> arrayList = new ArrayList<DAO>();
        testinfoDao = new TestInfoDAO( props, this );
        arrayList.add( testinfoDao );
        return arrayList;
    }

    @Override
    public int getPostSchemaCreationDBVersion() {
        return -1;
    }

    @Override
    public List<Migration> instantiateMigrations( Properties props ) {
        ArrayList<Migration> ms = new ArrayList<Migration>();
        ms.add( new Test2Migration() );
        ms.add( new Test1Migration() );
        return ms;
    }

    @Override
    public int getHighestMigrationNumber() {
        return 2;
    }

    @Override
    public int getLowestMigrationNumber() {
        return 1;
    }

    @Test
    public void testCreateOutput()
            throws IOException {
        extractCreateDBSchemaScript( System.out );
    }

    @Test
    public void testDropOutput()
            throws IOException {
        extractDropDBSchemaScript( System.out );
    }

}
