package org.cujau.db2.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.cujau.db2.jdbc.exceptions.CujauJDBCMappingException;

public class BulkUpdateTemplate {

    private DataSource ds;
    private String query;
    private Connection connection;
    private PreparedStatement stmt;
    private boolean canCleanupConnection;

    public BulkUpdateTemplate(DataSource ds, String query) {
        this.ds = ds;
        this.query = query;
    }

    public DataSource getDataSource() {
        return ds;
    }

    /**
     * Begin the bulk transaction, using a new connection.
     *
     * @return The connection object used by this bulk template.
     */
    public Connection begin() {
        return begin(null);
    }

    /**
     * Begin the bulk transaction. If a connection is given, it will be used for this work, but will
     * not be closed on {@link #end} or {@link #endWithoutCommit()}.
     *
     * @param conn
     *         The connection or <tt>null</tt> to create a new one.
     * @returns The connection used by this bulk template. If <tt>conn</tt> was null, the new
     * connection is returned, otherwise <tt>conn</tt> is returned.
     */
    public Connection begin(Connection conn) {
        try {
            if (conn == null) {
                connection = JDBCUtils.getConnection(getDataSource());
                connection.setAutoCommit(false);
                canCleanupConnection = true;
            } else {
                // If the connection was passed in, don't clean it up.
                // Someone else will do the cleaning up.
                connection = conn;
                canCleanupConnection = false;
            }
            stmt = JDBCUtils.createPreparedStatement(connection, query);
        } catch (SQLException e) {
            throw new CujauJDBCMappingException(e);
        }

        return connection;
    }

    public void update(Object... args) {
        try {
            JDBCUtils.fillPreparedStatement(stmt, args);
            stmt.executeUpdate();
            // Maybe auto commit after X rows?

        } catch (SQLException e) {
            throw new CujauJDBCMappingException(e);
        }
    }

    private void endWithoutCommit() {
        if (canCleanupConnection) {
            // Set it auto-commit back to default setting.
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                // Ignore as will probably get thrown in cleanup too.
            }
            JDBCUtils.cleanup(connection, stmt, null);
        } else {
            JDBCUtils.cleanup(null, stmt, null);
        }
    }

    public void end() {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new CujauJDBCMappingException(e);
        } finally {
            endWithoutCommit();
        }
    }

    public void endWithRollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new CujauJDBCMappingException(e);
        } finally {
            endWithoutCommit();
        }
    }
}
