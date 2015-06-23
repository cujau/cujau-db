package org.cujau.db2.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.cujau.db2.AbstractDBUtility;
import org.cujau.db2.H2DBUtilityHelpers;
import org.cujau.db2.dto.IdPrivateKeyDTO;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public abstract class AbstractInsertUpdateDAOTester<E extends IdPrivateKeyDTO> {

    protected AbstractDBUtility dbutil;

    protected abstract AbstractDBUtility createDBUtil();

    protected abstract AbstractInsertUpdateDAO<E> getDAO();

    protected abstract E createFirst();

    protected abstract void assertFirstFields( E o );

    protected abstract void changeFirst( E o );

    protected abstract void assertChangedFirstFields( E o );

    protected abstract E createSecond();

    protected int getDefaultItemCount() {
        return 0;
    }
    
    public AbstractDBUtility getDBUtil() {
        return dbutil;
    }
    
    @Before
    public void before()
            throws IOException {
        dbutil = createDBUtil();
        H2DBUtilityHelpers.initAndCreateInMemoryDB( dbutil );
    }

    @After
    public void after() {
        dbutil.dropDBSchema();
    }

    @Test
    public void testInsertSelect() {
        E o = createFirst();
        assertEquals( getDefaultItemCount() + 1, o.getId() );
        
        sleep( 50 );
        
        E o2 = getDAO().selectById( o.getId() );
        assertNotNull( o2 );
        assertFirstFields( o2 );
        assertTrue( o.equals( o2 ) );
    }

    @Test
    public void testSelectIdError() {
        assertNull( getDAO().selectById( 5 ) );
    }

    @Test
    public void testUpdateDelete() {
        E o = createFirst();
        assertEquals( getDefaultItemCount() + 1, o.getId() );

        changeFirst( o );
        E o2 = getDAO().selectById( o.getId() );
        assertNotNull( o2 );
        assertChangedFirstFields( o2 );
        assertTrue( o.equals( o2 ) );

        int deleteCt = getDAO().deleteById( o.getId() );
        assertEquals( 1, deleteCt );

        assertNull( getDAO().selectById( o.getId() ) );
    }

    @Test
    public void testSelectAllDeleteAll() {
        E o = createFirst();
        assertEquals( getDefaultItemCount() + 1, o.getId() );

        o = createSecond();
        assertEquals( getDefaultItemCount() + 2, o.getId() );

        assertEquals( getDefaultItemCount() + 2, getDAO().selectCount() );
        List<E> os = getDAO().selectAll();
        assertEquals( getDefaultItemCount() + 2, os.size() );

        assertEquals( getDefaultItemCount() + 2, getDAO().deleteAll() );
        os = getDAO().selectAll();
        assertEquals( 0, os.size() );
    }

    protected void sleep( long millis ) {
        try {
            Thread.sleep( millis );
        } catch ( InterruptedException e ) {
        }
    }
}
