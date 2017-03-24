package org.cujau.db2.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;

import org.cujau.db2.AbstractDBUtility;
import org.cujau.db2.SimpleTestDBUtility;
import org.cujau.db2.dto.SimpleTestDTO;

public class SimpleTestDAOTest extends AbstractInsertUpdateDAOTester<SimpleTestDTO> {

    @Override
    protected AbstractDBUtility createDBUtil() {
        return new SimpleTestDBUtility();
    }

    @Override
    protected AbstractInsertUpdateDAO<SimpleTestDTO> getDAO() {
        return ((SimpleTestDBUtility) dbutil).getSimpleTestDAO();
    }

    @Override
    protected SimpleTestDTO createFirst() {
        SimpleTestDTO o = new SimpleTestDTO();
        o.setName("TransparenTech LLC");
        o.setUseful(false);
        o.setCash(BigDecimal.valueOf(123.234));
        getDAO().insert(o);
        return o;
    }

    @Override
    protected void assertFirstFields(SimpleTestDTO o) {
        assertEquals("TransparenTech LLC", o.getName());
        assertFalse(o.isUseful());
        assertNull(o.getSymbol());
        assertTrue(BigDecimal.valueOf(123.234000000).compareTo(o.getCash()) == 0);
    }

    @Override
    protected void changeFirst(SimpleTestDTO o) {
        o.setName("Bob Smith's Car Wash");
        o.setUseful(true);
        o.setSymbol("SCW");
        o.setCash(BigDecimal.valueOf(543.654));
        getDAO().update(o);
    }

    @Override
    protected void assertChangedFirstFields(SimpleTestDTO o) {
        assertEquals("Bob Smith's Car Wash", o.getName());
        assertTrue(o.isUseful());
        assertEquals("SCW", o.getSymbol());
        assertTrue(BigDecimal.valueOf(543.654).compareTo(o.getCash()) == 0);
    }

    @Override
    protected SimpleTestDTO createSecond() {
        SimpleTestDTO o = new SimpleTestDTO();
        o.setName("Smith Car Wash SA");
        o.setUseful(false);
        o.setSymbol("SCW");
        o.setCash(BigDecimal.valueOf(435.0002435));
        getDAO().insert(o);
        return o;
    }

    @Test
    public void testSelect() {
        createFirst();
        createSecond();

        List<SimpleTestDTO> all = getDAO().selectAll();
        assertEquals(2, all.size());

        all = getDAO().selectAll("symbol = 'SCW'");
        assertEquals(1, all.size());

        all = getDAO().selectAll("is_useful = false");
        assertEquals(2, all.size());
    }
}
