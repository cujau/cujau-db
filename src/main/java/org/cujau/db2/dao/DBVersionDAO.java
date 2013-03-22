package org.cujau.db2.dao;

import java.util.Properties;

import org.cujau.db2.AbstractDBUtility;
import org.cujau.db2.DAOInitializationException;


public class DBVersionDAO extends DAO {

    public DBVersionDAO( Properties props, AbstractDBUtility dbutil ) throws DAOInitializationException {
        super( props, dbutil );
    }
    
    public int getVersion() {
        return template.queryForInt( getQuery( "getVersion" ) );
    }

    public void setVersion( int newVersion ) {
        template.update( getQuery( "setVersion" ), newVersion );
    }
    
}
