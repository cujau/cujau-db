package org.cujau.db2;

import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.Ignore;

/**
 * Unit testing helper methods.
 *
 * Mostly for date testing.
 */
@Ignore
public class DBUnitTestHelpers {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final SimpleDateFormat SDFTS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final SimpleDateFormat SDFSIMPLE = new SimpleDateFormat("yyyy-MM-dd");

    public static void assertTimestamp(String exp, Date orig) {
        String sorig = SDFTS.format(orig);
        assertTrue("Expecting '" + exp + "' but got '" + sorig + "'", sorig.equals(exp));
    }

    public static void assertDatetime(String exp, Date orig) {
        String sorig = SDF.format(orig);
        assertTrue("Expecting '" + exp + "' but got '" + sorig + "'", sorig.equals(exp));
    }

    public static void assertDate(String exp, Date orig) {
        String sorig = SDFSIMPLE.format(orig);
        assertTrue("Expecting '" + exp + "' but got '" + sorig + "'", sorig.equals(exp));
    }

    /**
     * Get a {@link Date} that is 100 milliseconds in the future.
     *
     * @return a Date
     */
    public static Date getFutureNow() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MILLISECOND, 100);
        return cal.getTime();
    }

    /**
     * Parse a timestamp string into a Date object.
     *
     * @param d
     *         The timestamp string in the format: "yyyy-MM-dd HH:mm:ss.SSS"
     * @return The given timestamp string as a Date object.
     */
    public static Date getTimestamp(String d) {
        try {
            return SDFTS.parse(d);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse a datetime string into a Date object.
     *
     * @param d
     *         The datetime string in the format: "yyyy-MM-dd HH:mm"
     * @return The given datetime string as a Date object.
     */
    public static Date getDatetime(String d) {
        try {
            return SDF.parse(d);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse a date string into a Date object.
     *
     * @param d
     *         The date string in the format: "yyyy-MM-dd"
     * @return The given date string as a Date object.
     */
    public static Date getDate(String d) {
        try {
            return SDFSIMPLE.parse(d);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String formatDatetime(Date d) {
        return SDF.format(d);
    }

    public static String formatDate(Date d) {
        return SDFSIMPLE.format(d);
    }
}
