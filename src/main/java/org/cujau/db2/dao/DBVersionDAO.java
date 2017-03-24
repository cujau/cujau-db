package org.cujau.db2.dao;

public interface DBVersionDAO extends DAO {
    int getVersion();

    void setVersion(int newVersion);
}
