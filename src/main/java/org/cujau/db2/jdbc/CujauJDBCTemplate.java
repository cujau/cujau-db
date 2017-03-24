package org.cujau.db2.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import org.cujau.db2.jdbc.exceptions.CujauJDBCEmptyResultSetException;
import org.cujau.db2.jdbc.exceptions.CujauJDBCException;
import org.cujau.db2.jdbc.exceptions.CujauJDBCExecutionException;
import org.cujau.db2.jdbc.exceptions.CujauJDBCMappingException;
import org.cujau.db2.jdbc.exceptions.CujauJDBCPreparationException;

public class CujauJDBCTemplate {

    private DataSource ds;

    public CujauJDBCTemplate(DataSource ds) {
        this.ds = ds;
    }

    public DataSource getDataSource() {
        return ds;
    }

    public void execute(String query) {
        Connection connection = null;
        Statement stmt = null;

        try {
            connection = JDBCUtils.getConnection(getDataSource());
            stmt = connection.createStatement();
        } catch (SQLException e) {
            JDBCUtils.cleanup(connection, stmt, null);
            throw new CujauJDBCPreparationException(e);
        }

        try {
            stmt.execute(query);
        } catch (SQLException e) {
            throw new CujauJDBCExecutionException(e);
        } finally {
            JDBCUtils.cleanup(connection, stmt, null);
        }
    }

    public long insertAndReturnId(String query, String idColumnName, List<String> columns, Map<String, Object> args) {
        Connection connection = JDBCUtils.getConnection(getDataSource());
        PreparedStatement stmt = JDBCUtils.prepareInsertStatement(connection, query, idColumnName, columns, args);

        long id = 0;
        try {
            stmt.executeUpdate();
            ResultSet rsKeys = stmt.getGeneratedKeys();
            rsKeys.next();
            // Since we only pass in 1 generated key name, there should be only 1 column
            // in the result. getLong( idColumnName ) doesn't work
            id = rsKeys.getLong(1);
        } catch (SQLException e) {
            throw new CujauJDBCExecutionException(e);
        } finally {
            JDBCUtils.cleanup(connection, stmt, null);
        }
        return id;
    }

    public int queryForInt(String query, Object... args) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = JDBCUtils.getConnection(getDataSource());
            stmt = JDBCUtils.prepareStatement(connection, query, args);
            rs = queryForResultSet(connection, stmt, true, true);

            return rs.getInt(1);
        } catch (SQLException e) {
            throw new CujauJDBCMappingException(e);
        } finally {
            JDBCUtils.cleanup(connection, stmt, rs);
        }
    }

    public long queryForLong(String query, Object... args) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = JDBCUtils.getConnection(getDataSource());
            stmt = JDBCUtils.prepareStatement(connection, query, args);
            rs = queryForResultSet(connection, stmt, true, true);

            return rs.getLong(1);
        } catch (SQLException e) {
            throw new CujauJDBCMappingException(e);
        } finally {
            JDBCUtils.cleanup(connection, stmt, rs);
        }
    }

    public Date queryForDate(String query, Object... args) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = JDBCUtils.getConnection(getDataSource());
            stmt = JDBCUtils.prepareStatement(connection, query, args);
            rs = queryForResultSet(connection, stmt, true, true);

            return rs.getTimestamp(1);
        } catch (SQLException e) {
            throw new CujauJDBCMappingException(e);
        } finally {
            JDBCUtils.cleanup(connection, stmt, rs);
        }
    }

    public <T> T queryForObject(String query, boolean checkForEmptyResultSet, boolean exceptionOnEmptyResultSet,
                                TypedRowMapper<T> mapper, Object... args) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = JDBCUtils.getConnection(getDataSource());
            stmt = JDBCUtils.prepareStatement(connection, query, args);
            rs = queryForResultSet(connection, stmt, checkForEmptyResultSet, exceptionOnEmptyResultSet);
            if (rs != null) {
                return mapper.mapRow(rs, 1);
            }
        } catch (SQLException e) {
            throw new CujauJDBCMappingException(e);
        } finally {
            JDBCUtils.cleanup(connection, stmt, rs);
        }

        return null;
    }

    public <T> T queryForObject(String query, TypedRowMapper<T> mapper, Object... args) {
        return queryForObject(query, true, true, mapper, args);
    }

    public int update(String query, Object... args) {
        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = JDBCUtils.getConnection(getDataSource());
            stmt = JDBCUtils.prepareStatement(connection, query, args);

            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CujauJDBCExecutionException(e);
        } finally {
            JDBCUtils.cleanup(connection, stmt, null);
        }
    }

    public int update(String query, List<String> columns, Map<String, Object> args) {
        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = JDBCUtils.getConnection(getDataSource());
            stmt = JDBCUtils.prepareStatement(connection, query, columns, args);

            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CujauJDBCExecutionException(e);
        } finally {
            JDBCUtils.cleanup(connection, stmt, null);
        }
    }

    public <T> List<T> query(String query, TypedRowMapper<T> mapper, Object... args)
            throws CujauJDBCException {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = JDBCUtils.getConnection(getDataSource());
            stmt = JDBCUtils.prepareStatement(connection, query, args);
            rs = queryForResultSet(connection, stmt, true, false);

            ArrayList<T> ret = new ArrayList<T>();
            if (rs == null) {
                // rs is null when there were no results selected.
                return ret;
            }
            do {
                T t = mapper.mapRow(rs, rs.getRow());
                ret.add(t);
            } while (rs.next());
            return ret;
        } catch (SQLException e) {
            throw new CujauJDBCMappingException(e);
        } finally {
            JDBCUtils.cleanup(connection, stmt, rs);
        }
    }

    public int query(String query, ResultSetRowHandler handler, Object... args) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = JDBCUtils.getConnection(getDataSource());
            stmt = JDBCUtils.prepareStatement(connection, query, args);
            rs = queryForResultSet(connection, stmt, true, false);

            int ret = 0;
            if (rs == null) {
                // rs is null when there were no results selected.
                return ret;
            }
            do {
                handler.handleRow(rs, rs.getRow());
                ret++;
            } while (rs.next());
            return ret;
        } catch (SQLException e) {
            throw new CujauJDBCMappingException(e);
        } finally {
            JDBCUtils.cleanup(connection, stmt, rs);
        }
    }

    public void query(String query, ResultSetHandler handler, Object... args) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = JDBCUtils.getConnection(getDataSource());
            stmt = JDBCUtils.prepareStatement(connection, query, args);
            rs = queryForResultSet(connection, stmt, false, false);

            if (rs == null) {
                // rs is null when there were no results selected.
                return;
            }

            rs.beforeFirst();
            handler.handleResultSet(rs);
        } catch (SQLException e) {
            throw new CujauJDBCMappingException(e);
        } finally {
            JDBCUtils.cleanup(connection, stmt, rs);
        }
    }

    private ResultSet queryForResultSet(Connection connection, PreparedStatement stmt, boolean checkForEmptyResultSet,
                                        boolean exceptionOnEmptyResultSet) {
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery();
        } catch (SQLException e) {
            JDBCUtils.cleanup(null, null, rs);
            throw new CujauJDBCExecutionException(e);
        }
        try {
            if (checkForEmptyResultSet) {
                if (!rs.next()) {
                    JDBCUtils.cleanup(null, null, rs);
                    if (exceptionOnEmptyResultSet) {
                        throw new CujauJDBCEmptyResultSetException();
                    } else {
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            JDBCUtils.cleanup(null, null, rs);
            throw new CujauJDBCExecutionException(e);
        }
        return rs;
    }

}
