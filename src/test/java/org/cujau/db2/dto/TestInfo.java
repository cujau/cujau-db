package org.cujau.db2.dto;

import org.junit.Ignore;

@Ignore
public class TestInfo {

    private int id;
    private String key;
    private int val;
    private String prio;

    public int getId() {
        return id;
    }

    public void setId( int id ) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey( String key ) {
        this.key = key;
    }

    public int getVal() {
        return val;
    }

    public void setVal( int val ) {
        this.val = val;
    }

    public String getPrio() {
        return prio;
    }

    public void setPrio( String prio ) {
        this.prio = prio;
    }
}
