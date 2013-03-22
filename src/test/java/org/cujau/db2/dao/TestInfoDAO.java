package org.cujau.db2.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.cujau.db2.AbstractDBUtility;
import org.cujau.db2.DAOInitializationException;
import org.cujau.db2.dto.TestInfo;
import org.cujau.db2.jdbc.TypedRowMapper;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore
public class TestInfoDAO extends DAO {

    private static final Logger LOG = LoggerFactory.getLogger( TestInfoDAO.class );

    public TestInfoDAO( Properties props, AbstractDBUtility dbutil )
            throws DAOInitializationException {
        super( props, dbutil );
    }

    public void insert( String key, int val ) {
        template.update( getQuery( "insert" ), key, val );
    }

    public void insertPostMigrate( String key, int val, String prio ) {
        template.update( getQuery( "insertPostMigrate" ), key, val, prio );
    }

    public List<TestInfo> selectAll() {
        return template.query( getQuery( "selectAll" ), new TypedRowMapper<TestInfo>() {

            @Override
            public TestInfo mapRow( ResultSet rs, int rowNum )
                    throws SQLException {
                TestInfo i = new TestInfo();
                i.setId( rs.getInt( "id" ) );
                i.setKey( rs.getString( "key" ) );
                i.setVal( rs.getInt( "val" ) );
                if ( rs.getMetaData().getColumnCount() > 3 ) {
                    i.setPrio( rs.getString( "prio" ) );
                    LOG.debug( "Have 'prio' column." );
                }
                return i;
            }

        } );
    }
}
