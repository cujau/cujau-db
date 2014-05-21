package org.cujau.db2.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;

import org.cujau.db2.AbstractDBUtility;
import org.cujau.db2.SimpleTestDBUtility;
import org.cujau.db2.dao.AbstractInsertUpdateDAO;
import org.cujau.db2.dto.SimpleTestDTO;

public class SimpleTestDAOTest extends AbstractInsertUpdateDAOTester<SimpleTestDTO> {

    @Override
    protected AbstractDBUtility createDBUtil() {
        return new SimpleTestDBUtility();
    }

    @Override
    protected AbstractInsertUpdateDAO<SimpleTestDTO> getDAO() {
        return ( (SimpleTestDBUtility) dbutil ).getSimpleTestDAO();
    }

    @Override
    protected SimpleTestDTO createFirst() {
        SimpleTestDTO o = new SimpleTestDTO();
        o.setName( "Bedag Informatik SA" );
        o.setUseful( false );
        o.setCash( BigDecimal.valueOf( 123.234 ) );
        getDAO().insert( o );
        return o;
    }

    @Override
    protected void assertFirstFields( SimpleTestDTO o ) {
        assertEquals( "Bedag Informatik SA", o.getName() );
        assertFalse( o.isUseful() );
        assertNull( o.getSymbol() );
        assertTrue( BigDecimal.valueOf( 123.234000000 ).compareTo( o.getCash() ) == 0 );
    }

    @Override
    protected void changeFirst( SimpleTestDTO o ) {
        o.setName( "Bob Smith's Car Wash" );
        o.setUseful( true );
        o.setSymbol( "SCW" );
        o.setCash( BigDecimal.valueOf( 543.654 ) );
        getDAO().update( o );
    }

    @Override
    protected void assertChangedFirstFields( SimpleTestDTO o ) {
        assertEquals( "Bob Smith's Car Wash", o.getName() );
        assertTrue( o.isUseful() );
        assertEquals( "SCW", o.getSymbol() );
        assertTrue( BigDecimal.valueOf( 543.654 ).compareTo( o.getCash() ) == 0 );
    }

    @Override
    protected SimpleTestDTO createSecond() {
        SimpleTestDTO o = new SimpleTestDTO();
        o.setName( "Smith Car Wash SA" );
        o.setUseful( false );
        o.setSymbol( "SCW" );
        o.setCash( BigDecimal.valueOf( 435.0002435 ) );
        getDAO().insert( o );
        return o;
    }

}
