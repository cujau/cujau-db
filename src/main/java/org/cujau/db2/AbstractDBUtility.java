package org.cujau.db2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.cujau.db2.dao.DAO;
import org.cujau.db2.dao.DBVersionDAO;
import org.cujau.db2.jdbc.CujauJDBCTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDBUtility {

    private static final Logger LOG = LoggerFactory.getLogger( AbstractDBUtility.class );
    public static final String DB_PREFIX_PROPERTY = "db.prefix";
    static final String QUERY_SEPARATOR = ";";
    static final Pattern QUERY_SEPARATOR_PATTERN = Pattern.compile( QUERY_SEPARATOR );

    protected DataSource dataSource;
    protected Properties props;
    protected CujauJDBCTemplate template;

    private ArrayList<IDBSchemaListener> schemaListeners = new ArrayList<IDBSchemaListener>();
    private String dialect;
    private List<DAO> daos;
    private DBVersionDAO dbversion;

    public String getDialect() {
        return dialect;
    }

    /**
     * Set the prefix to use for table names. Typically you would use "myprefix_", but special
     * possible values here are:
     * <ul>
     * <li><tt>empty</tt> - The empty prefix ("") will be specifically set.</li>
     * <li><tt>none</tt> or <tt>null</tt> - No prefix will be set.</li>
     * </ul>
     * 
     * @param prefix
     */
    public void setTablePrefix( String prefix ) {
        if ( prefix == null ) {
            return;
        }
        if ( prefix.startsWith( "\"" ) || prefix.startsWith( "'" ) ) {
            prefix = prefix.substring( 1 );
        }
        if ( prefix.endsWith( "\"" ) || prefix.endsWith( "'" ) ) {
            prefix = prefix.substring( 0, prefix.length() - 1 );
        }
        prefix = prefix.trim();
        if ( prefix.equals( "empty" ) ) {
            getProps().setProperty( DB_PREFIX_PROPERTY, "" );
        } else if ( !prefix.equals( "" ) && !prefix.equals( "none" ) && !prefix.equals( "null" ) ) {
            getProps().setProperty( DB_PREFIX_PROPERTY, prefix.trim() );
        }
    }

    public String getDbPrefixProperty() {
        return getProps().getProperty( DB_PREFIX_PROPERTY );
    }

    public void setDialect( String dialect )
            throws IOException {
        this.dialect = dialect;

        String name = AbstractDBUtility.class.getName();
        String pkgName = name.substring( 0, name.lastIndexOf( "." ) ).replace( ".", "/" );
        pkgName = "/" + pkgName + "/";

        // Load the properties for this dialect. Any any values already specified in the "props"
        // object are taken as overrides to the default properties defined in the resource
        // properties file.
        Properties defaultProps = new Properties();
        String dialectFile = pkgName + dialect + ".properties";
        InputStream dialectStream = AbstractDBUtility.class.getResourceAsStream( dialectFile );
        if ( dialectStream == null ) {
            LOG.error( "Could not load {}", dialectFile );
        }
        defaultProps.load( dialectStream );
        getProps();
        if ( props != null ) {
            defaultProps.putAll( props );
        }
        props = defaultProps;
    }

    public void setDataSource( DataSourceWithDialect ds )
            throws IOException {
        setDialect( ds.getDialect() );
        setDataSource( (DataSource) ds );
    }

    public void setDataSource( DataSource ds ) {
        if ( dialect == null ) {
            throw new IllegalStateException( "The database dialect must be set before the dataSource." );
        }

        dataSource = ds;
        daos = new ArrayList<DAO>();
        template = new CujauJDBCTemplate( dataSource );

        try {
            dbversion = new DBVersionDAO( props, this );
            daos.add( dbversion );

            daos.addAll( instantiateDAOs( props ) );
        } catch ( DAOInitializationException e ) {
            LOG.error( e.getMessage(), e );
        }

        for ( DAO dao : daos ) {
            dao.setDataSource( dataSource );
            LOG.debug( "Set data source for: {}", dao.getClass().getSimpleName() );
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public abstract List<DAO> instantiateDAOs( Properties props )
            throws DAOInitializationException;

    public DBVersionDAO getDBVersionDAO() {
        return dbversion;
    }

    public int getDBVersion() {
        return dbversion.getVersion();
    }

    public List<DAO> getDAOs() {
        return daos;
    }

    /*
     * public void createDBSchema() { for ( DAO dao : daos ) { if ( dao.getCreateQuery() != null ) {
     * LOG.info( "Creating {}", dao.getClass().getSimpleName() ); template.execute(
     * dao.getCreateQuery() ); } } }
     * 
     * public void dropDBSchema() { dropDBSchema( false ); }
     * 
     * public void dropDBSchema( boolean continueOnError ) { ListIterator<DAO> iterator =
     * daos.listIterator( daos.size() ); while ( iterator.hasPrevious() ) { DAO dao =
     * iterator.previous(); if ( dao.getCreateQuery() != null ) { try { LOG.info(
     * "Dropping {} : {}", dao.getClass().getSimpleName(), dao.getDropQuery() ); template.execute(
     * dao.getDropQuery() ); } catch ( RuntimeException t ) { LOG.error( "Exception on drop.", t );
     * if ( !continueOnError ) { throw t; } } } } }
     * 
     * public CujauJDBCTemplate getMigrationTemplate() { return template; }
     */

    /**
     * Return the value that the db_version table should be set to after the complete schema
     * creation. The db_version table will be updated to this value after the schema has been
     * created.
     * <p>
     * Returning -1 means that the db_version table will not be updated.
     * </p>
     * 
     * @return The value to set in the db_version table after the schema has been created.
     */
    protected abstract int getPostSchemaCreationDBVersion();

    /**
     * Returns a list of Migration objects.
     * 
     * @param props
     * @return The list of Migration objects or <tt>null</tt> if no migrations are necessary.
     * @throws MigrationInitializationException
     */
    public abstract List<Migration> instantiateMigrations( Properties props )
            throws MigrationInitializationException;

    /**
     * The lowest migration number (i.e. the last migration) to be used in a down migration.
     * Typically the lowest supported schema version number.
     * 
     * @return
     */
    public abstract int getLowestMigrationNumber();

    /**
     * The highest migration number to which a schema will be migrated up. Typically this is the
     * same as the {@link #getPostSchemaCreationDBVersion} value.
     * 
     * @return
     */
    public abstract int getHighestMigrationNumber();

    public void createDBSchema() {
        // Call the pre schema creation listeners to determine if we can continue with schema
        // creation.
        boolean allowedToCreate = true;
        for ( IDBSchemaListener listener : schemaListeners ) {
            allowedToCreate &= listener.preSchemaCreate( this );
        }

        if ( allowedToCreate ) {
            // Create the schema.
            for ( DAO dao : daos ) {
                splitThenExecuteQuery( dao.getCreateQuery() );
            }
            // Update the db_version table.
            if ( getPostSchemaCreationDBVersion() != -1 ) {
                dbversion.setVersion( getPostSchemaCreationDBVersion() );
            }
            // Call the post schema creation listeners.
            for ( IDBSchemaListener listener : schemaListeners ) {
                listener.postSchemaCreate( this );
            }
        }
    }

    public void migrateDBSchemaUp() {
        migrateDBSchemaUp( getHighestMigrationNumber() );
    }

    public void migrateDBSchemaUp( int highestMigrationNumber ) {
        int dbv = getDBVersion();
        LOG.info( "DBVersion={}", dbv );

        if ( dbv < highestMigrationNumber ) {
            boolean allowedToMigrate = true;
            for ( IDBSchemaListener listener : schemaListeners ) {
                allowedToMigrate &= listener.preSchemaMigrateUp( this );
            }
            if ( !allowedToMigrate ) {
                return;
            }

            // Get the list of migrations.
            List<Migration> migrations = getSortedMigrations( 1 );
            // Loop through the migrations executing the ones that have not yet been applied.
            for ( Migration m : migrations ) {
                if ( m.getMigrationNumber() <= dbv ) {
                    // Skipping migration that has already been applied.
                    continue;
                }
                if ( m.getMigrationNumber() > highestMigrationNumber ) {
                    // Skipping migration that should not be applied.
                    continue;
                }
                // Apply migration.
                List<String> sqlStmts = m.up( this );
                if ( sqlStmts != null ) {
                    for ( String stmt : sqlStmts ) {
                        splitThenExecuteQuery( stmt );
                    }
                }
                // Update the DBVersion number.
                dbversion.setVersion( m.getMigrationNumber() );
                LOG.info( "Migrated up to version {}", m.getMigrationNumber() );
                // Notify any migration listeners.
                for ( IDBSchemaListener listener : schemaListeners ) {
                    listener.postSchemaMigrateUp( m.getMigrationNumber(), this );
                }
            }

            for ( IDBSchemaListener listener : schemaListeners ) {
                listener.postSchemaMigrateUp( this );
            }
        }
    }

    public void migrateDBSchemaDown() {
        int dbv = getDBVersion();
        LOG.info( "DBVersion={}", dbv );

        if ( dbv >= getLowestMigrationNumber() ) {
            boolean allowedToMigrate = true;
            for ( IDBSchemaListener listener : schemaListeners ) {
                allowedToMigrate &= listener.preSchemaMigrateDown( this );
            }
            if ( !allowedToMigrate ) {
                return;
            }

            // Get the list of migrations.
            List<Migration> migrations = getSortedMigrations( -1 );
            // Loop through the migrations executing the ones that have not yet been applied.
            for ( Migration m : migrations ) {
                if ( m.getMigrationNumber() > dbv ) {
                    // Skipping migration that has already been applied.
                    continue;
                }
                // Apply migration.
                List<String> sqlStmts = m.down( this );
                if ( sqlStmts != null ) {
                    for ( String stmt : sqlStmts ) {
                        splitThenExecuteQuery( stmt );
                    }
                }
                // Update the DBVersion number.
                dbversion.setVersion( m.getMigrationNumber() - 1 );
                LOG.info( "Migrated up to version {}", m.getMigrationNumber() - 1 );
                // Notify any migration listeners.
                for ( IDBSchemaListener listener : schemaListeners ) {
                    listener.postSchemaMigrateDown( m.getMigrationNumber(), this );
                }
            }

            for ( IDBSchemaListener listener : schemaListeners ) {
                listener.postSchemaMigrateDown( this );
            }
        }
    }

    public void dropDBSchema() {
        boolean allowedToDrop = true;
        for ( IDBSchemaListener listener : schemaListeners ) {
            allowedToDrop &= listener.preSchemaDrop( this );
        }

        if ( allowedToDrop ) {
            ListIterator<DAO> iterator = daos.listIterator( daos.size() );
            while ( iterator.hasPrevious() ) {
                DAO dao = iterator.previous();
                splitThenExecuteQuery( dao.getDropQuery() );
            }

            for ( IDBSchemaListener listener : schemaListeners ) {
                listener.postSchemaDrop( this );
            }
        }

    }

    public boolean addSchemaListener( IDBSchemaListener listener ) {
        if ( listener == null ) {
            return false;
        }
        return schemaListeners.add( listener );
    }

    public boolean removeSchemaListener( IDBSchemaListener listener ) {
        if ( listener == null ) {
            return false;
        }
        return schemaListeners.remove( listener );
    }

    public List<IDBSchemaListener> getSchemaListeners() {
        return schemaListeners;
    }

    public CujauJDBCTemplate getJDBCTemplate() {
        return template;
    }

    public void extractCreateDBSchemaScript( OutputStream output )
            throws IOException {
        BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( output, "UTF-8" ) );
        writer.write( "-- " );
        writer.newLine();
        writer.write( "-- Schema creation script for " + this.getClass().getName() );
        writer.newLine();
        writer.write( "-- " );
        writer.newLine();
        writer.newLine();
        for ( DAO dao : daos ) {
            writer.write( "-- " + dao.getClass().getName() );
            writer.newLine();
            writer.write( dao.getCreateQuery() );
            writer.newLine();
            writer.newLine();
        }
        writer.flush();
    }

    public void extractDropDBSchemaScript( OutputStream output )
            throws IOException {
        BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( output, "UTF-8" ) );
        writer.write( "-- " );
        writer.newLine();
        writer.write( "-- Schema removal script for " + this.getClass().getName() );
        writer.newLine();
        writer.write( "-- " );
        writer.newLine();
        writer.newLine();
        ListIterator<DAO> iterator = daos.listIterator( daos.size() );
        while ( iterator.hasPrevious() ) {
            DAO dao = iterator.previous();
            writer.write( "-- " + dao.getClass().getName() );
            writer.newLine();
            writer.write( dao.getDropQuery() );
            writer.newLine();
            writer.newLine();
        }
        writer.flush();
    }

    public Properties getProps() {
        if ( props == null ) {
            props = new Properties();
            addDefaultUserProperties( props );
        }
        return props;
    }

    public String getDatabaseAndDriverInfo() {
        StringBuilder buf = new StringBuilder();
        Connection con = null;
        try {
            con = getDataSource().getConnection();
            DatabaseMetaData metadata = con.getMetaData();
            buf.append( metadata.getDatabaseProductName() ).append( "(" );
            buf.append( metadata.getDatabaseProductVersion() ).append( "); " );
            buf.append( metadata.getDriverName() ).append( "(" );
            buf.append( metadata.getDriverVersion() ).append( ")" );
        } catch ( SQLException e ) {
            buf.append( e.getMessage() );
        } finally {
            if ( con != null ) {
                try {
                    con.close();
                } catch ( SQLException e ) {
                }
            }
        }
        return buf.toString();
    }

    protected void addDefaultUserProperties( Properties ps ) {
        // Set the default prefix property to the empty value. This means that, by default, the
        // db.prefix properties in queries will be removed.
        ps.setProperty( DB_PREFIX_PROPERTY, "" );
    }

    private void splitThenExecuteQuery( String bigQuery ) {
        if ( bigQuery == null || bigQuery.trim().equals( "" ) ) {
            // Don't try to execute null or empty queries.
            return;
        }
        if ( bigQuery.indexOf( QUERY_SEPARATOR ) != -1 ) {
            for ( String query : QUERY_SEPARATOR_PATTERN.split( bigQuery ) ) {
                template.execute( query );
            }
        } else {
            template.execute( bigQuery );
        }
    }

    private List<Migration> getSortedMigrations( final int multiplier )
            throws MigrationInitializationException {
        // Get the list of migrations.
        List<Migration> migrations = instantiateMigrations( props );
        if ( migrations == null ) {
            // Not really a good situation if you get here, so just return an empty list to avoid
            // bigger problems.
            return new ArrayList<Migration>();
        }
        // Sort the migrations based on their migrationNumber.
        Collections.sort( migrations, new Comparator<Migration>() {
            @Override
            public int compare( Migration o1, Migration o2 ) {
                return multiplier * o1.getMigrationNumber().compareTo( o2.getMigrationNumber() );
            }
        } );
        return migrations;
    }

}
