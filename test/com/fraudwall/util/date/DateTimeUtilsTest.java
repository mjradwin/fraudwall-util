/**
 * Copyright (c) 2010, Anchor Intelligence. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the
 *   distribution.
 *
 * - Neither the name of Anchor Intelligence nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package com.fraudwall.util.date;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;

import com.fraudwall.util.AbstractAnchorTest;
import com.fraudwall.util.date.DateTimeUtils;


/**
 * Tests the {@link DateTimeUtils} implementation.
 *
 * @author Allan Heydon
 */
public class DateTimeUtilsTest extends AbstractAnchorTest {
	private static final String DATE_2003_08_12 = "2003-08-12";
	private static final String DATE_12_AUG_2003 = "12/Aug/2003";
	private static final String TIME_23_34_45 = "23:34:45";
	private static final Date DATE_2003_08_12_23_34_45 =
		makeDate(2003, 8, 12, 23, 34, 45, "UTC");
	private static final Date DATE_2003_08_12_00_00_00 =
		makeDate(2003, 8, 12, 0, 0, 0, "UTC");

	/** {@link DateTimeUtils#dateFromApacheTimestamp(String)} ------------------------------------- */

	public void testDateFromApacheTimestampThrowsParseExceptionForIllegalDateFormat() {
		checkDateFromApacheTimestampThrowsParseExceptionForIllegalDate("2003-08-12");
		checkDateFromApacheTimestampThrowsParseExceptionForIllegalDate("12/Aug/03");
	}

	private void checkDateFromApacheTimestampThrowsParseExceptionForIllegalDate(String dateStr) {
		checkDateFromApacheTimestampThrowsParseException(dateStr + ":" + TIME_23_34_45 + " -0000");
	}

	public void testDateFromApacheTimestampThrowsParseExceptionForIllegalTimeFormat() {
		checkDateFromApacheTimestampThrowsParseExceptionForIllegalTime("23-34-45");
		checkDateFromApacheTimestampThrowsParseExceptionForIllegalTime("23-3-45");
		checkDateFromApacheTimestampThrowsParseExceptionForIllegalTime("23-34-4");
	}

	private void checkDateFromApacheTimestampThrowsParseExceptionForIllegalTime(String timeStr) {
		checkDateFromApacheTimestampThrowsParseException(DATE_12_AUG_2003 + ":" + timeStr + " -0000");
	}

	public void testDateFromApacheTimestampThrowsParseExceptionForIllegalTimeZone() {
		checkDateFromApacheTimestampThrowsParseExceptionForIllegalTimeZone("0000");
		checkDateFromApacheTimestampThrowsParseExceptionForIllegalTimeZone("-01");
		checkDateFromApacheTimestampThrowsParseExceptionForIllegalTimeZone("-013");
		checkDateFromApacheTimestampThrowsParseExceptionForIllegalTimeZone("+01");
		checkDateFromApacheTimestampThrowsParseExceptionForIllegalTimeZone("+013");
	}

	private void checkDateFromApacheTimestampThrowsParseExceptionForIllegalTimeZone(String tzStr) {
		checkDateFromApacheTimestampThrowsParseException(DATE_12_AUG_2003 + ":" + TIME_23_34_45 + " " + tzStr);
	}

	private void checkDateFromApacheTimestampThrowsParseException(String dateTimeStr) {
		try {
			DateTimeUtils.dateFromApacheTimestamp(dateTimeStr);
			fail();
		} catch (ParseException ex) {
			// expected case
			String expMsg = "Unparseable date: \"" + dateTimeStr + "\"";
			assertEquals(expMsg, ex.getMessage());
		}
	}

	public void testDateFromApacheTimestampReturnsCorrectDate() throws Exception {
		String dateTimeString = DATE_12_AUG_2003 + ":" + TIME_23_34_45 + " -0000";
		Date d = DateTimeUtils.dateFromApacheTimestamp(dateTimeString);
		assertEquals(DATE_2003_08_12_23_34_45, d);
	}

	/** {@link DateTimeUtils#dateFromIISTimestamp(String, String)} ------------------------------- */

	public void testDateFromIISTimestampThrowsParseExceptionForIllegalDateFormat() {
		checkDateFromIISTimestampThrowsParseExceptionForIllegalDate("2003/08/12");
		checkDateFromIISTimestampThrowsParseExceptionForIllegalDate("03-08-12");
		checkDateFromIISTimestampThrowsParseExceptionForIllegalDate("08-12-2003");
		checkDateFromIISTimestampThrowsParseExceptionForIllegalDate("2003-8-12");
		checkDateFromIISTimestampThrowsParseExceptionForIllegalDate("2003-08-1");
	}

	private void checkDateFromIISTimestampThrowsParseExceptionForIllegalDate(String dateStr) {
		checkDateFromIISTimestampThrowsParseException(dateStr, TIME_23_34_45);
	}

	public void testDateFromIISTimestampThrowsParseExceptionForIllegalTimeFormat() {
		checkDateFromIISTeimstampThrowsParseExceptionForIllegalTime("23-34-45");
		checkDateFromIISTeimstampThrowsParseExceptionForIllegalTime("23:34");
		checkDateFromIISTeimstampThrowsParseExceptionForIllegalTime("23:34:1");
		checkDateFromIISTeimstampThrowsParseExceptionForIllegalTime("23:3:45");
		checkDateFromIISTeimstampThrowsParseExceptionForIllegalTime("3:34:45");
	}

	private void checkDateFromIISTeimstampThrowsParseExceptionForIllegalTime(String string) {
		checkDateFromIISTimestampThrowsParseException(DATE_2003_08_12, string);
	}

	private void checkDateFromIISTimestampThrowsParseException(String dateStr,
			String timeStr) {
		try {
			DateTimeUtils.dateFromIISTimestamp(dateStr, timeStr);
			fail();
		} catch (ParseException ex) {
			// expected case
			String expMsg = "Unparseable date: \"" + dateStr + " " + timeStr + " -0000\"";
			assertEquals(expMsg, ex.getMessage());
		}
	}

	public void testDateFromIISTimestampReturnsCorrectDate() throws Exception {
		Date d = DateTimeUtils.dateFromIISTimestamp(DATE_2003_08_12, TIME_23_34_45);
		assertEquals(DATE_2003_08_12_23_34_45, d);
	}

	public void testDateFromIISTimestampAndUtcDateFromMillisAreInverses() throws Exception {
		Date d = DateTimeUtils.dateFromIISTimestamp(DATE_2003_08_12, TIME_23_34_45);
		String dt_str = DateTimeUtils.utcDateTimeFromMillis(d.getTime());
		assertEquals(DATE_2003_08_12 + " " + TIME_23_34_45, dt_str);
	}

	/** {@link DateTimeUtils#utcDateTimeFromMillis(long)} --------------------------------------- */

	public void testUtcDateTimeFromMillisReturnsCorrectString() {
		long timeInMillis = DATE_2003_08_12_23_34_45.getTime();
		String got = DateTimeUtils.utcDateTimeFromMillis(timeInMillis);
		assertEquals(DATE_2003_08_12 + " " + TIME_23_34_45, got);
	}

	public void testUtcDateTimeFromMillisReturnsNullWhenTimeIsLessThanOrEqualToZero() {
		assertNull(DateTimeUtils.utcDateTimeFromMillis(-1));
		assertNull(DateTimeUtils.utcDateTimeFromMillis(-5000));
		assertNull(DateTimeUtils.utcDateTimeFromMillis(0));
	}

	public void testUtcDateTimeFromMillisAndDateFromIISTimestampAreInverses() throws Exception {
		long timeInMillis = DATE_2003_08_12_23_34_45.getTime();
		String dt_str = DateTimeUtils.utcDateTimeFromMillis(timeInMillis);
		String[] dt_strs = dt_str.split(" ");
		Date date = DateTimeUtils.dateFromIISTimestamp(dt_strs[0], dt_strs[1]);
		assertEquals(timeInMillis, date.getTime());
	}

	/** {@link DateTimeUtils#utcDateFromMillis(long)} -------------------------------------------- */

	public void testUtcDateFromMillisReturnsCorrectString() {
		long timeInMillis = DATE_2003_08_12_23_34_45.getTime();
		String got = DateTimeUtils.utcDateFromMillis(timeInMillis);
		assertEquals(DATE_2003_08_12, got);
	}

	public void testUtcDateFromMillisReturnsNullWhenTimeIsNegative() {
		assertNull(DateTimeUtils.utcDateFromMillis(-1));
	}

	/** {@link DateTimeUtils#utcDateTimeToMillis(String)} -------------------------------------- */

	public void testUtcDateTimeToMillisReturnsCorrectValue() {
		long got = DateTimeUtils.utcDateTimeToMillis(DATE_2003_08_12 + " " + TIME_23_34_45);
		assertEquals(DATE_2003_08_12_23_34_45.getTime(), got);
	}

	public void testUtcDateTimeToMillisReturnsNullTimeWhenDateIsNullEmptyOrBlank() {
		assertEquals(DateTimeUtils.NULL_TIME, DateTimeUtils.utcDateTimeToMillis(null));
		assertEquals(DateTimeUtils.NULL_TIME, DateTimeUtils.utcDateTimeToMillis(""));
		assertEquals(DateTimeUtils.NULL_TIME, DateTimeUtils.utcDateTimeToMillis("  "));
	}

	public void testUtcDateTimeToMillisReturnsNullTimeWhenDateIsNullString() {
		assertEquals(DateTimeUtils.NULL_TIME, DateTimeUtils.utcDateTimeToMillis("NULL"));
	}

	/** {@link DateTimeUtils#utcDateToMillis(String)} ------------------------------------------ */

	public void testUtcDateToMillisReturnsCorrectValue() {
		long got = DateTimeUtils.utcDateToMillis(DATE_2003_08_12);
		assertEquals(DATE_2003_08_12_00_00_00.getTime(), got);
	}

	public void testUtcDateToMillisReturnsNullTimeWhenDateIsNullEmptyOrBlank() {
		assertEquals(DateTimeUtils.NULL_TIME, DateTimeUtils.utcDateToMillis(null));
		assertEquals(DateTimeUtils.NULL_TIME, DateTimeUtils.utcDateToMillis(""));
		assertEquals(DateTimeUtils.NULL_TIME, DateTimeUtils.utcDateToMillis("  "));
	}

	public void testUtcDateToMillisReturnsNullTimeWhenDateIsNullString() {
		assertEquals(DateTimeUtils.NULL_TIME, DateTimeUtils.utcDateToMillis("NULL"));
	}

	/** {@link DateTimeUtils#toTimestampFromLong(long)} ---------------------------------------- */

	public void testToTimestampFromLongReturnsNullIfArgumentIsNegative() {
		assertNull(DateTimeUtils.toTimestampFromLong(-1));
	}

	public void testToTimestampFromLongReturnsTimestampWithTheSameTime() {
		assertEquals(new Timestamp(0L), DateTimeUtils.toTimestampFromLong(0L));
		assertEquals(new Timestamp(12345678L), DateTimeUtils.toTimestampFromLong(12345678L));
		final Date now = new Date();
		assertEquals(new Timestamp(now.getTime()), DateTimeUtils.toTimestampFromLong(now.getTime()));
	}

	/** {@link DateTimeUtils#longFromTimestamp(Timestamp)} ------------------------------------- */

	public void testLongFromTimestampReturnsNullTimeConstantOnNullArgument() {
		assertEquals(DateTimeUtils.NULL_TIME, DateTimeUtils.longFromTimestamp(null));
	}

	public void testLongFromTimestampReturnsCorrectValueForNonNullArgument() {
		assertEquals(0L, DateTimeUtils.longFromTimestamp(new Timestamp(0L)));
		assertEquals(12345678L, DateTimeUtils.longFromTimestamp(new Timestamp(12345678L)));
		final Date now = new Date();
		assertEquals(now.getTime(), DateTimeUtils.longFromTimestamp(new Timestamp(now.getTime())));
	}

	/** {@link DateTimeUtils#durationFromSQL(int)} --------------------------------------------- */

	public void testDurationFromSQLReturnsNullTimeForNegativeArgument() {
		assertEquals(DateTimeUtils.NULL_TIME, DateTimeUtils.durationFromSQL(-1));
	}

	public void testDurationFromSQLCorrectlyConvertsSecondsToMilliseconds() {
		assertEquals(0L, DateTimeUtils.durationFromSQL(0));
		assertEquals(1000L, DateTimeUtils.durationFromSQL(1));
		assertEquals(123000L, DateTimeUtils.durationFromSQL(123));
	}

	/** {@link DateTimeUtils#durationToSQL(long)} ---------------------------------------------- */

	public void testDurationToSqlIsIdentityFunctionForNegativeArguments() {
		assertEquals(-1, DateTimeUtils.durationToSQL(-1L));
		assertEquals(-123, DateTimeUtils.durationToSQL(-123L));
		assertEquals(-540, DateTimeUtils.durationToSQL(-540L));
	}

	public void testDurationToSqlConvertsMillisecondsToSecondsForSecondAlignedMilliseconds() {
		assertEquals(1, DateTimeUtils.durationToSQL(1000L));
		assertEquals(123, DateTimeUtils.durationToSQL(123000L));
	}

	public void testDurationToSqlTruncatesPartialSeconds() {
		assertEquals(1, DateTimeUtils.durationToSQL(1111L));
		assertEquals(123, DateTimeUtils.durationToSQL(123999L));
	}

	/** {@link DateTimeUtils#durationFromString(String)} ---------------------------------------- */

	public void testDurationFromStringReturnsNullTimeConstantOnNullArgument() {
		assertEquals(DateTimeUtils.NULL_TIME, DateTimeUtils.durationFromString(null));
	}

	public void testDurationFromStringReturnsCorrectValueForNonNullArgument() {
		assertEquals(0, DateTimeUtils.durationFromString("0"));
		assertEquals(1000, DateTimeUtils.durationFromString("1"));
		assertEquals(123000, DateTimeUtils.durationFromString("123"));
		assertEquals(-1, DateTimeUtils.durationFromString("-540000"));
	}

	/** {@link DateTimeUtils#durationToString(long)} -------------------------------------------- */

	public void testDurationToStringReturnsSecondsFromSecondAlignedMilliseconds() {
		assertEquals("0", DateTimeUtils.durationToString(0L));
		assertEquals("1", DateTimeUtils.durationToString(1000L));
		assertEquals("123", DateTimeUtils.durationToString(123000L));
		assertEquals("-1", DateTimeUtils.durationToString(-540000L));
	}

	public void testDurationToStringTruncatesPartialSeconds() {
		assertEquals("1", DateTimeUtils.durationToString(1111L));
		assertEquals("123", DateTimeUtils.durationToString(123999L));
		assertEquals("-1", DateTimeUtils.durationToString(-540888L));
	}

	/** {@link DateTimeUtils#dateFloor(Date)} --------------------------------------------------- */

	public void testDateFloorReturnsDateWithTimeTruncated() {
		Date date   = makeDate(2010, 7, 5, 4, 32, 21, "UTC");
		Date expected = makeDate(2010, 7, 5, 0,  0,  0, "UTC");
		assertEquals(expected, DateTimeUtils.dateFloor(date));
	}

	public void testDateFloorReturnsNullWhenNullPassedIn() {
		assertNull(DateTimeUtils.dateFloor(null));
	}

	/** {@link DateTimeUtils#dateFloor(long)} --------------------------------------------------- */

	public void testDateFloorReturnsTimestampWithTimeTruncated() {
		long timestamp = makeTimeInMillis(2010, 7, 5, 4, 32, 21, "UTC");
		long expected  = makeTimeInMillis(2010, 7, 5, 0,  0,  0, "UTC");
		assertEquals(expected, DateTimeUtils.dateFloor(timestamp));
	}

	public void testDateFloorReturnsZeronWhenZeroPassedIn() {
		assertEquals(0, DateTimeUtils.dateFloor(0));
	}
}
