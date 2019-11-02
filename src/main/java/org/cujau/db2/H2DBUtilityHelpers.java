package org.cujau.db2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.dbcp2.BasicDataSource;

import org.cujau.utils.FileUtil;

/**
 * Helpers for setting up in-memory and file-based H2 databases that can be used in unit tests or
 * local development testing.
 */
public class H2DBUtilityHelpers {

    public static AbstractDBUtility initAndCreateInMemoryDB(AbstractDBUtility dbutil)
            throws IOException {
        return initAndCreateInMemoryDB(dbutil, null);
    }

    public static AbstractDBUtility initAndCreateInMemoryDB(AbstractDBUtility dbutil, String prefix)
            throws IOException {
        if (dbutil == null) {
            throw new IllegalStateException("dbutil can not be null");
        }
        dbutil.setDialect("h2");
        if (prefix != null) {
            dbutil.setTablePrefix(prefix);
        }

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("org.h2.Driver");
        // The DB_CLOSE_DELAY=-1 means the contents of an in-memory database will be kept as long
        // as the virtual machine is alive. Otherwise, they are lost when the last connection to the
        // in-memory db is closed.
        ds.setUrl("jdbc:h2:mem:cujaustddb-"
                  + System.currentTimeMillis());// MODE=MSSQLServer;DB_CLOSE_DELAY=-1;TRACE_LEVEL_FILE=4"
        ds.setUsername("");
        ds.setPassword("");
        dbutil.setDataSource(ds);
        dbutil.createDBSchema();
        return dbutil;
    }

    public static AbstractDBUtility initAndCreateFileDB(AbstractDBUtility dbutil, String dbName, boolean createSchema)
            throws IOException {
        File dbDir = new File(System.getProperty("java.io.tmpdir") + "/" + dbName);
        if (dbDir.exists()) {
            throw new IllegalStateException(dbDir.getAbsolutePath());
        }
        return initAndCreateFileDB(dbutil, dbDir, null, createSchema);
    }

    public static AbstractDBUtility initAndCreateFileDB(AbstractDBUtility dbutil, File dbDir, String prefix,
                                                        boolean createSchema)
            throws IOException {
        if (dbutil == null) {
            throw new IllegalStateException("dbutil can not be null");
        }
        dbutil.setDialect("h2");
        if (prefix != null) {
            dbutil.setTablePrefix(prefix);
        }

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl("jdbc:h2:file:" + dbDir.getAbsolutePath() + "");// ;TRACE_LEVEL_FILE=4"
        ds.setUsername("");
        ds.setPassword("");
        dbutil.setDataSource(ds);
        if (createSchema) {
            if (dbDir.exists()) {
                FileUtil.deleteDirectory(dbDir);
            }
            dbutil.createDBSchema();
        }
        return dbutil;
    }

    public static void extractSchema(AbstractDBUtility dbutil, File createSchemaOutputFile, File dropSchemaOutputFile)
            throws IOException {
        FileOutputStream out = new FileOutputStream(createSchemaOutputFile);
        dbutil.extractCreateDBSchemaScript(out);
        out.close();

        out = new FileOutputStream(dropSchemaOutputFile);
        dbutil.extractDropDBSchemaScript(out);
        out.close();
    }
}
