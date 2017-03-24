package org.cujau.db2;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.cujau.db2.dao.DAO;
import org.cujau.db2.dao.SimpleTestDAO;

public class SimpleTestDBUtility extends AbstractDBUtilityImpl {

    private SimpleTestDAO simpleDao;

    @Override
    public List<DAO> instantiateDAOs(Properties props)
            throws DAOInitializationException {
        ArrayList<DAO> ret = new ArrayList<DAO>();

        simpleDao = new SimpleTestDAO(props, this);
        ret.add(simpleDao);

        return ret;
    }

    public SimpleTestDAO getSimpleTestDAO() {
        return simpleDao;
    }

    @Override
    protected int getPostSchemaCreationDBVersion() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<Migration> instantiateMigrations(Properties props)
            throws MigrationInitializationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getLowestMigrationNumber() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getHighestMigrationNumber() {
        // TODO Auto-generated method stub
        return 0;
    }
}
