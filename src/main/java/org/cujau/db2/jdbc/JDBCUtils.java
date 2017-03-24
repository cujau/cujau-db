package org.cujau.db2.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import org.cujau.db2.jdbc.exceptions.CujauJDBCCleanupException;
import org.cujau.db2.jdbc.exceptions.CujauJDBCConnectionException;
import org.cujau.db2.jdbc.exceptions.CujauJDBCPreparationException;

final class JDBCUtils {

    static Connection getConnection(DataSource ds) {
        Connection connection = null;
        try {
            connection = ds.getConnection();
        } catch (SQLException e) {
            cleanup(connection, null, null);
            throw new CujauJDBCConnectionException(e);
        }
        return connection;
    }

    static PreparedStatement prepareStatement(Connection connection, String query, Object... args) {
        PreparedStatement stmt = createPreparedStatement(connection, query);
        fillPreparedStatement(stmt, args);
        return stmt;
    }

    static PreparedStatement prepareStatement(Connection connection, String query, List<String> columns,
                                              Map<String, Object> args) {
        PreparedStatement stmt = createPreparedStatement(connection, query);
        fillPreparedStatement(stmt, columns, args);
        return stmt;
    }

    static PreparedStatement prepareInsertStatement(Connection connection, String query, String idColumn,
                                                    List<String> columns, Map<String, Object> args) {
        PreparedStatement stmt = createPreparedStatement(connection, query, idColumn);
        fillPreparedStatement(stmt, columns, args);
        return stmt;
    }

    static PreparedStatement createPreparedStatement(Connection connection, String query) {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(query);
        } catch (SQLException e) {
            cleanup(null, stmt, null);
            throw new CujauJDBCPreparationException(e);
        }
        return stmt;
    }

    static PreparedStatement createPreparedStatement(Connection connection, String query, String idColumn) {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(query, new String[] {idColumn});
        } catch (SQLException e) {
            cleanup(null, stmt, null);
            throw new CujauJDBCPreparationException(e);
        }
        return stmt;
    }

    static PreparedStatement fillPreparedStatement(PreparedStatement stmt, List<String> columns,
                                                   Map<String, Object> args) {
        if (args == null) {
            return stmt;
        }
        int ct = 0;
        try {
            for (String col : columns) {
                Object arg = args.get(col);
                ct++;
                setValue(stmt, ct, arg);
            }
        } catch (SQLException e) {
            cleanup(null, stmt, null);
            throw new CujauJDBCPreparationException(e);
        }
        return stmt;
    }

    static PreparedStatement fillPreparedStatement(PreparedStatement stmt, Object... args) {
        if (args == null) {
            return stmt;
        }
        int ct = 0;
        try {
            for (Object arg : args) {
                ct++;
                setValue(stmt, ct, arg);
            }
        } catch (SQLException e) {
            cleanup(null, stmt, null);
            throw new CujauJDBCPreparationException(e);
        }
        return stmt;
    }

    // This is ugly, but is more performant than a more elegant solution.
    //
    // You can performance test this with JDBCUtilsTest. The more elegant solution is commented out
    // below (#setValue2 and CujauJDBCType enum).
    //
    private static void setValue(PreparedStatement stmt, int ct, Object arg)
            throws SQLException {
        if (arg instanceof String) {
            stmt.setString(ct, (String) arg);
        } else if (arg instanceof Integer) {
            stmt.setInt(ct, (Integer) arg);
        } else if (arg instanceof BigDecimal) {
            stmt.setBigDecimal(ct, (BigDecimal) arg);
        } else if (arg instanceof Double) {
            stmt.setDouble(ct, (Double) arg);
        } else if (arg instanceof Date) {
            stmt.setTimestamp(ct, new Timestamp(((Date) arg).getTime()));
        } else if (arg instanceof Boolean) {
            stmt.setBoolean(ct, (Boolean) arg);
        } else if (arg instanceof Long) {
            stmt.setLong(ct, (Long) arg);
        } else if (arg instanceof Byte) {
            stmt.setByte(ct, (Byte) arg);
        } else if (arg instanceof Float) {
            stmt.setFloat(ct, (Float) arg);
        } else if (arg == null) {
            stmt.setNull(ct, Types.NULL);
        } else {
            // ignore it.
        }
    }

    static void cleanup(Connection connection, Statement stmt, ResultSet rs) {
        CujauJDBCCleanupException ex = null;
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            ex = new CujauJDBCCleanupException(e);
        }
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            ex = new CujauJDBCCleanupException(e);
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            ex = new CujauJDBCCleanupException(e);
        }
        if (ex != null) {
            throw ex;
        }
    }

    // This solution is less performance than the ugly mega-if-else.
    // 
    // private static void setValue2( PreparedStatement stmt, int ct, Object arg )
    // throws SQLException {
    // CujauJDBCTypes type = CujauJDBCTypes.fromClass( arg );
    // switch ( type ) {
    // case BIGDECIMAL:
    // stmt.setBigDecimal( ct, (BigDecimal) arg );
    // break;
    // case BOOLEAN:
    // stmt.setBoolean( ct, (Boolean) arg );
    // break;
    // case BYTE:
    // stmt.setByte( ct, (Byte) arg );
    // break;
    // case DATE:
    // stmt.setTimestamp( ct, new Timestamp( ( (Date) arg ).getTime() ) );
    // break;
    // case DOUBLE:
    // stmt.setDouble( ct, (Double) arg );
    // break;
    // case FLOAT:
    // stmt.setFloat( ct, (Float) arg );
    // break;
    // case INTEGER:
    // stmt.setInt( ct, (Integer) arg );
    // break;
    // case LONG:
    // stmt.setLong( ct, (Long) arg );
    // break;
    // case NULL:
    // stmt.setNull( ct, Types.NULL );
    // break;
    // case STRING:
    // stmt.setString( ct, (String) arg );
    // break;
    // default:
    // break;
    // }
    // }
    //
    // private static enum CujauJDBCTypes {
    // STRING( String.class ), //
    // INTEGER( Integer.class ), //
    // BIGDECIMAL( BigDecimal.class ), //
    // DOUBLE( Double.class ), //
    // DATE( Date.class ), //
    // BOOLEAN( Boolean.class ), //
    // LONG( Long.class ), //
    // BYTE( Byte.class ), //
    // FLOAT( Float.class ), //
    // NULL( null );
    //
    // private static final Map<Class<?>, CujauJDBCTypes> types = new HashMap<Class<?>,
    // CujauJDBCTypes>();
    // static {
    // for ( CujauJDBCTypes tp : values() ) {
    // if ( tp.klass != null ) {
    // types.put( tp.klass, tp );
    // }
    // }
    // }
    //
    // public static CujauJDBCTypes fromClass( Object arg ) {
    // if ( arg == null ) {
    // return NULL;
    // }
    // return types.get( arg.getClass() );
    // }
    //
    // private Class<?> klass;
    //
    // CujauJDBCTypes( Class<?> klass ) {
    // this.klass = klass;
    // }
    // }
}
