package org.cujau.db2.dao;

import java.util.List;

public class QueryBuilder {

    public static String buildInsertQuery(String table, List<String> columns, String idColumnName, boolean withId) {
        StringBuilder buf = new StringBuilder();
        buf.append("INSERT INTO ").append(table).append("(");
        StringBuilder places = new StringBuilder();
        for (String col : columns) {
            if (withId || !col.equals(idColumnName)) {
                buf.append(col).append(",");
                places.append("?,");
            }
        }
        buf.deleteCharAt(buf.length() - 1);
        places.deleteCharAt(places.length() - 1);
        buf.append(") VALUES(").append(places.toString()).append(")");
        return buf.toString();
    }

    public static String buildUpdateQuery(String table, List<String> columns, String idColumnName) {
        StringBuilder buf = new StringBuilder();
        buf.append("UPDATE ").append(table).append(" SET ");
        for (String col : columns) {
            if (!col.equals(idColumnName)) {
                buf.append(col).append("=?,");
            }
        }
        buf.deleteCharAt(buf.length() - 1);
        buf.append(" WHERE ").append(idColumnName).append("=?");
        return buf.toString();
    }

}
