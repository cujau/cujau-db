package org.cujau.db2.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.cujau.db2.AbstractDBUtility;
import org.cujau.db2.DAOInitializationException;
import org.cujau.db2.dto.IdPrivateKeyDTO;
import org.cujau.db2.jdbc.CujauJDBCTemplate;
import org.cujau.db2.jdbc.TypedRowMapper;
import org.cujau.db2.jdbc.exceptions.CujauJDBCEmptyResultSetException;

/**
 * Abstract base class for all DAOs whose DTO objects implement the {@link IdPrivateKeyDTO}
 * interface. Provides the {@link #save}, {@link #insert}, {@link #update},
 * {@link #deleteById(long)}, {@link #deleteAll()}, {@link #selectAll} and {@link #selectCount}
 * methods.
 * 
 * <p>
 * Extending classes <em>must</em> have the following queries defined in their XSQL file:
 * <ul>
 * <li>&lt;createQuery&gt;</li>
 * <li>&lt;dropQuery&gt;</li>
 * <li>&lt;query id="update"&gt;</li>
 * </ul>
 * </p>
 * 
 * @param <E>
 *            A DTO class that implements {@link IdentityDTO}.
 */
public abstract class AbstractInsertUpdateDAO<E extends IdPrivateKeyDTO> extends DAO {

    public static final String ID_COLUMN_NAME = "id";

    protected TypedRowMapper<E> mapper;
    protected String fullTableName;
    protected String insertQuery;
    protected String insertQueryWithId;
    protected String updateQuery;
    protected CujauJDBCTemplate simpleInsert;
    // protected NamedParameterJdbcTemplate namedTemplate;
    protected CujauJDBCTemplate simpleInsertWithoutGeneratedKey;
    protected List<String> columnNames;
    protected List<String> columnNamesWithoutId;

    public AbstractInsertUpdateDAO( Properties props, AbstractDBUtility dbutil )
            throws DAOInitializationException {
        super( props, dbutil );
    }

    public String getInsertQuery() {
        return insertQuery;
    }

    @Override
    public void setDataSource( DataSource dataSource ) {
        super.setDataSource( dataSource );

        simpleInsert = new CujauJDBCTemplate( dataSource );// new SimpleJdbcInsert( dataSource );
        // simpleInsert.withTableName( getTableName() );
        // simpleInsert.usingGeneratedKeyColumns( "id" );

        // simpleInsertWithoutGeneratedKey = new SimpleJdbcInsert( dataSource );
        // simpleInsertWithoutGeneratedKey.withTableName( getTableName() );

        // namedTemplate = new NamedParameterJdbcTemplate( dataSource );

        columnNamesWithoutId = getColumnNames();
        columnNames = new ArrayList<String>( columnNamesWithoutId );
        columnNames.add( ID_COLUMN_NAME );

        insertQuery = QueryBuilder.buildInsertQuery( getTableName(), columnNames, ID_COLUMN_NAME, false );
        insertQueryWithId =
            QueryBuilder.buildInsertQuery( getTableName(), columnNames, ID_COLUMN_NAME, true );
        updateQuery = QueryBuilder.buildUpdateQuery( getTableName(), columnNames, ID_COLUMN_NAME );
        mapper = createRowMapper();
    }

    public void save( E dto ) {
        if ( dto.getId() == -1 ) {
            insert( dto );
        } else {
            update( dto );
        }
    }

    /**
     * Insert the given object and return the new Id of the object.
     * 
     * @param obj
     *            The IdPrivateKeyDTO based object to insert.
     * @return The Id of the newly inserted object.
     */
    public long insert( E obj ) {
        long newId =
            simpleInsert.insertAndReturnId( insertQuery, ID_COLUMN_NAME, columnNamesWithoutId,
                                            buildInsertUpdateParams( obj, false ) );
        obj.setId( newId );
        return newId;
    }

    public void insertWithId( E obj ) {
        simpleInsert.insertAndReturnId( insertQueryWithId, ID_COLUMN_NAME, columnNames,
                                        buildInsertUpdateParams( obj, true ) );
    }

    public int update( E obj ) {
        return simpleInsert.update( updateQuery, columnNames, buildInsertUpdateParams( obj, true ) );
    }

    /**
     * Select the object with the given Id.
     * 
     * @param id
     *            The Id of the object to return.
     * @return The object with the given Id or <tt>null</tt> if no object with the given Id was
     *         found.
     */
    public E selectById( long id ) {
        try {
            return template.queryForObject( "SELECT * FROM " + getTableName() + " WHERE id = ?", mapper, id );
        } catch ( CujauJDBCEmptyResultSetException e ) {
            return null;
        }
    }

    /**
     * Delete the object with the given Id.
     * 
     * @param id
     *            the Id of the object to delete.
     * @return The number of rows deleted.
     */
    public int deleteById( long id ) {
        return template.update( "DELETE FROM " + getTableName() + " WHERE id = ?", id );
    }

    /**
     * Delete all rows from the table.
     * 
     * @return The number of rows deleted.
     */
    public int deleteAll() {
        return template.update( "DELETE FROM " + getTableName() );
    }

    /**
     * Seelct all items from the table. If an 'order by' clause is specified for this table via the
     * {@link #getSelectAllOrderByClause()}, the items will be ordered accordingly.
     * 
     * @return A list of all items in this table.
     */
    public List<E> selectAll() {
        return doSelectWithOrderBy( "SELECT * FROM " + getTableName() );
    }

    /**
     * Select all items from the table that match the given <tt>whereClause</tt>.
     * 
     * @param whereClause
     *            The SQL 'where' clause to use when selecting items. It should not include the
     *            'where' keyword itself, just the conditions (i.e. "a = 5 AND b = 'xyz'").
     * @return A list of items that matched the query. If an 'order by' clause is specified for this
     *         table (via the {@link #getSelectAllOrderByClause()}), the items will be ordered
     *         accordingly.
     */
    public List<E> selectAll( String whereClause ) {
        if ( whereClause == null || whereClause.isEmpty() ) {
            return selectAll();
        }

        return doSelectWithOrderBy( "SELECT * FROM " + getTableName() + " where " + whereClause );
    }

    protected List<E> doSelectWithOrderBy( String query ) {
        String orderBy = getSelectAllOrderByClause();
        if ( orderBy == null ) {
            return doSelect( query );
        } else {
            return doSelect( query + " " + orderBy );
        }
    }

    protected List<E> doSelect( String query ) {
        return template.query( query, mapper );
    }

    public long selectCount() {
        return template.queryForLong( "SELECT count(*) FROM " + getTableName() );
    }

    public long selectMaxId() {
        return template.queryForLong( "SELECT max( id ) FROM " + getTableName() );
    }

    public String getTableName() {
        if ( fullTableName == null ) {
            String prefix = getPrefix();
            fullTableName = prefix + getBaseTableName();
        }
        return fullTableName;
    }

    /**
     * Returns the name of the database table, sans prefix, that is managed by this DAO class.
     * 
     * @return A String containing the name of this DAO's database table.
     */
    protected abstract String getBaseTableName();

    /**
     * Returns a List of the columns in this table.
     * <p>
     * NOTE: do not include the 'id' column. It will automatically be added.
     * </p>
     * 
     * @return A List of column name Strings.
     */
    protected abstract List<String> getColumnNames();

    /**
     * Returns a {@link MapSqlParameterSource} object filled with the field data from the given DTO
     * object. The parameter names set in the MapSqlParameterSource object must correspond to the
     * parameter names used in the "update" query in the DAO's XSQL file.
     * 
     * @param dtoObj
     *            The DTO object whose fields should be added to the returned parameter source
     *            object.
     * @param params
     *            A partially filled parameter source to be filled. Normally is the return value of
     *            this method.
     * @return A {@link Map<String,Object>} object filled with the parameters of the dtoObj.
     */
    protected abstract Map<String, Object> fillParams( E dtoObj, Map<String, Object> params );

    /**
     * Returns a new instance of the class that can map from a resultSet row to the DTO class
     * instance.
     * 
     * @return A Spring JDBC {@link ParameterizedRowMapper}.
     */
    protected abstract TypedRowMapper<E> createRowMapper();

    protected void mapIdentityRow( E row, ResultSet rs, int rowNum )
            throws SQLException {
        row.setId( rs.getLong( "id" ) );
    }

    /**
     * An ORDER BY clause to use in the selectAll method. Should include the "ORDER BY " part.
     * 
     * @return
     */
    protected String getSelectAllOrderByClause() {
        return null;
    }

    private Map<String, Object> buildInsertUpdateParams( E dtoObj, boolean withId ) {

        Map<String, Object> params = new HashMap<String, Object>();
        if ( withId ) {
            params.put( "id", dtoObj.getId() );
        }
        return fillParams( dtoObj, params );
    }

    public static Integer getNullableIntColumn( ResultSet rs, String columnName )
            throws SQLException {
        Integer ret = rs.getInt( columnName );
        if ( rs.wasNull() ) {
            return null;
        }
        return ret;
    }

    public static Long getNullableLongColumn( ResultSet rs, String columnName )
            throws SQLException {
        Long ret = rs.getLong( columnName );
        if ( rs.wasNull() ) {
            return null;
        }
        return ret;
    }

    public static String getNullableStringColumn( ResultSet rs, String columnName )
            throws SQLException {
        String ret = rs.getString( columnName );
        if ( rs.wasNull() ) {
            return null;
        }
        return ret;
    }

    public static Boolean getNullableBooleanColumn( ResultSet rs, String columnName )
            throws SQLException {
        Boolean ret = rs.getBoolean( columnName );
        if ( rs.wasNull() ) {
            return null;
        }
        return ret;
    }

    public static Date getNullableDateColumn( ResultSet rs, String columnName )
            throws SQLException {
        Timestamp ts = rs.getTimestamp( columnName );
        if ( rs.wasNull() ) {
            return null;
        }
        return new Date( ts.getTime() );
    }

    public static Double getNullableDoubleColumn( ResultSet rs, String columnName )
            throws SQLException {
        Double ret = rs.getDouble( columnName );
        if ( rs.wasNull() ) {
            return null;
        }
        return ret;
    }
}
