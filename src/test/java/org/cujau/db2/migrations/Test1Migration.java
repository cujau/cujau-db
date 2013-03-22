package org.cujau.db2.migrations;

import java.util.ArrayList;
import java.util.List;

import org.cujau.db2.AbstractDBUtility;
import org.cujau.db2.Migration;
import org.junit.Ignore;

@Ignore
public class Test1Migration implements Migration {

    @Override
    public Integer getMigrationNumber() {
        return 1;
    }

    @Override
    public List<String> up( AbstractDBUtility dbutil ) {
        ArrayList<String> ops = new ArrayList<String>();
        ops.add( "DELETE FROM test_info WHERE key = 'two'" );
        return ops;
    }

    @Override
    public List<String> down( AbstractDBUtility dbutil ) {
        ArrayList<String> ops = new ArrayList<String>();
        ops.add( "INSERT INTO test_info (key,val) VALUES ( 'two', 2 )" );
        return ops;
    }
}
