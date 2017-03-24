package org.cujau.db2.dao;

import java.util.Properties;

import org.cujau.db2.AbstractDBUtility;
import org.cujau.db2.DAOInitializationException;

public class DBVersionDAOImpl extends DAOImpl implements DBVersionDAO {

    public DBVersionDAOImpl(Properties props, AbstractDBUtility dbutil)
            throws DAOInitializationException {
        super(props, dbutil);
    }

    @Override
    public int getVersion() {
        return template.queryForInt(getQuery("getVersion"));
    }

    @Override
    public void setVersion(int newVersion) {
        template.update(getQuery("setVersion"), newVersion);
    }

}
