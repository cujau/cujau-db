package org.cujau.db2.migrations;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;

import org.cujau.db2.AbstractDBUtility;
import org.cujau.db2.Migration;

@Ignore
public class Test2Migration implements Migration {

    @Override
    public Integer getMigrationNumber() {
        return 2;
    }

    @Override
    public List<String> up(AbstractDBUtility dbutil) {
        ArrayList<String> ops = new ArrayList<String>();
        ops.add("ALTER TABLE test_info ADD prio VARCHAR( 256 )");
        return ops;
    }

    @Override
    public List<String> down(AbstractDBUtility dbutil) {
        ArrayList<String> ops = new ArrayList<String>();
        ops.add("ALTER TABLE test_info DROP COLUMN prio");
        return ops;
    }
}
