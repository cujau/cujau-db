package org.cujau.db2.dao;

import javax.sql.DataSource;

import org.cujau.db2.AbstractDBUtility;

public interface DAO {
    AbstractDBUtility getDBUtility();

    String getPrefix();

    void setDataSource(DataSource dataSource);

    String getCreateQuery();

    String getDropQuery();

    String getQuery(String id);
}
