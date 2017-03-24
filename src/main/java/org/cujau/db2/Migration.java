package org.cujau.db2;

import java.util.List;

/**
 * Database migration interface. Classes that implement this interface provide migration information
 * for a specific, sequential migration number.
 */
public interface Migration {

    /**
     * Get the sequential order number of this migration.
     * <p>
     * Migrations should be ordered sequentially and no two migrations should have the same
     * migration number. The very first migration should have the number 1.
     * </p>
     * <p>
     * After a migration has been migrated up, the db_version will be set to this number. After a
     * migration has been migrated down, the db_version will be set to this number - 1.
     * </p>
     *
     * @return An integer specifying this migration's sequential order.
     */
    Integer getMigrationNumber();

    /**
     * Get a list of SQL statements that migrate the database up to the state that the database is
     * required to be in after this Migration has occured.
     *
     * @param dbutil
     *         The database utility object.
     * @return A List of SQL migration statements.
     */
    List<String> up(AbstractDBUtility dbutil);

    /**
     * Get a list of SQL statements that migrate the database down to the state that the database
     * was before this Migration occured.
     * <p>
     * These statements should undo any changes described by the statements returned from the
     * {@link #up} method.
     * </p>
     *
     * @param dbutil
     *         The database utility object.
     * @return A List of SQL migration statements.
     */
    List<String> down(AbstractDBUtility dbutil);

}
