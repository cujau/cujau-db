package org.cujau.db2;

import javax.sql.DataSource;

public interface DataSourceWithDialect extends DataSource {

    String getDialect();

    String getUrl();

    void setUrl(String jdbcUrl);

    void setUsername(String val);

    void setPassword(String val);
}
