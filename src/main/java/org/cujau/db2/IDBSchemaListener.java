package org.cujau.db2;

/**
 * Interfce for classes wishing to receive notification of events occuring on the DB schema.
 */
public interface IDBSchemaListener {

    /**
     * Notification that the DB schema is about to be created.
     * 
     * @param dbutil
     *            The {@link AbstractDBUtility} that will create the schema.
     * @return <tt>true</tt> if this listener agrees to allow the creation. <tt>false</tt> vetoes
     *         the creation.
     */
    boolean preSchemaCreate( AbstractDBUtility dbutil );

    /**
     * Notification that the DB schema was created. This method will not be called if the
     * {@link #preSchemaCreate} method vetoed (returned <tt>false</tt>) the creation.
     * 
     * @param dbutil
     *            The {@link AbstractDBUtility} that created the schema.
     */
    void postSchemaCreate( AbstractDBUtility dbutil );

    boolean preSchemaMigrateUp( AbstractDBUtility dbutil );

    /**
     * Notification that the given migration has been applied successfully. This will only be called
     * for migrations that are actually executed. If a migration is skipped, for example because it
     * has already been applied, this method will not be called.
     * 
     * @param migrationNumber
     *            The number of the migration that was applied successfully.
     * @param dbutil
     *            The {@link AbstractDBUtility} that executed the migration.
     */
    void postSchemaMigrateUp( int migrationNumber, AbstractDBUtility dbutil );

    /**
     * Notification that all necessary migrations have been applied successfully. This will only be
     * called if at least 1 migration was necessary and if migrations were allowed to be executed
     * (which is determined by the return value of all listener's {@link #preSchemaMigrateUp}
     * method).
     * 
     * @param dbutil
     *            The {@link AbstractDBUtility} that executed the migration.
     */
    void postSchemaMigrateUp( AbstractDBUtility dbutil );

    boolean preSchemaMigrateDown( AbstractDBUtility dbutil );

    /**
     * Notification that the given migration has been unapplied successfully. This will only be
     * called for migrations that are actually executed. If a migration is skipped, for example
     * because it has already been unapplied, this method will not be called.
     * 
     * @param migrationNumber
     *            The number of the migration that was successfully unapplied.
     * @param dbutil
     *            The {@link AbstractDBUtility} that executed the migration.
     */
    void postSchemaMigrateDown( int migrationNumber, AbstractDBUtility dbutil );

    void postSchemaMigrateDown( AbstractDBUtility dbutil );

    /**
     * Notification that the DB schema is about to be dropped.
     * 
     * @param dbutil
     *            The {@link AbstractDBUtility} that will drop the schema.
     * @return <tt>true</tt> if this listener agrees to allow the dropping of the schema.
     *         <tt>false</tt> vetoes the drop.
     */
    boolean preSchemaDrop( AbstractDBUtility dbutil );

    /**
     * Notification that the DB schema was dropped. This method will not be called if the
     * {@link #preSchemaDrop} method vetoed (returned <tt>false</tt>) the drop.
     * 
     * @param dbutil
     *            The {@link AbstractDBUtility} that dropped the schema.
     */
    void postSchemaDrop( AbstractDBUtility dbutil );

}
