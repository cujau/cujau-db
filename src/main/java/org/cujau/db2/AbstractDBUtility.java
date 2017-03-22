package org.cujau.db2;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;

import org.cujau.db2.dao.DAO;
import org.cujau.db2.dao.DBVersionDAO;
import org.cujau.db2.jdbc.CujauJDBCTemplate;

public interface AbstractDBUtility {
    String DB_PREFIX_PROPERTY = "db.prefix";

    String getDialect();

    void setTablePrefix(String prefix);

    String getDbPrefixProperty();

    void setDialect(String dialect)
            throws IOException;

    void setDataSource(DataSourceWithDialect ds)
                    throws IOException;

    void setDataSource(DataSource ds);

    DataSource getDataSource();

    List<DAO> instantiateDAOs(Properties props)
                            throws DAOInitializationException;

    DBVersionDAO getDBVersionDAO();

    int getDBVersion();

    List<DAO> getDAOs();

    /**
     * Returns a list of Migration objects.
     *
     * @param props
     *         The properties to use during migration.
     * @return The list of Migration objects or <tt>null</tt> if no migrations are necessary.
     * @throws MigrationInitializationException
     */
    List<Migration> instantiateMigrations(Properties props)
            throws MigrationInitializationException;

    /**
     * The lowest migration number (i.e. the last migration) to be used in a down migration.
     * Typically the lowest supported schema version number.
     *
     * @return The lowest supported schema version number.
     */
    int getLowestMigrationNumber();

    /**
     * The highest migration number to which a schema will be migrated up. Typically this is the
     * same as the {@link #getPostSchemaCreationDBVersion} value.
     *
     * @return The highest supported schema version number.
     */
    int getHighestMigrationNumber();

    void createDBSchema();

    boolean needsMigration();

    boolean needsMigration(int highestMigrationNumber);

    void migrateDBSchemaUp();

    void migrateDBSchemaUp(int highestMigrationNumber);

    void migrateDBSchemaDown();

    void dropDBSchema();

    boolean addSchemaListener(IDBSchemaListener listener);

    boolean removeSchemaListener(IDBSchemaListener listener);

    List<IDBSchemaListener> getSchemaListeners();

    CujauJDBCTemplate getJDBCTemplate();

    void extractCreateDBSchemaScript(OutputStream output)
            throws IOException;

    void extractDropDBSchemaScript(OutputStream output)
                    throws IOException;

    Properties getProps();

    String getDatabaseAndDriverInfo();
}
