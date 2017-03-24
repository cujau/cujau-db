package org.cujau.db2.dao;

import java.util.List;
import javax.sql.DataSource;

import org.cujau.db2.dto.IdPrivateKeyDTO;

public interface AbstractInsertUpdateDAO<E extends IdPrivateKeyDTO> extends DAO {
    String getInsertQuery();

    boolean save(E dto);

    long insert(E obj);

    int update(E obj);

    E selectById(long id);

    int deleteById(long id);

    int deleteAll();

    List<E> selectAll();

    List<E> selectAll(String whereClause);

    long selectCount();

    String getTableName();
}
