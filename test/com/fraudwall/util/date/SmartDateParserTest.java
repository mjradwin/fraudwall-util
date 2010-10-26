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

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.NoOpLog;

import com.fraudwall.util.AbstractAnchorTest;
import com.fraudwall.util.date.SmartDateParser;

/**
 * Tests the {@link SmartDateParser} implementation.
 */
public class SmartDateParserTest extends AbstractAnchorTest {

	// ------------------------------------- parseDate

	public void testParseDateThrowsExceptionOnEmptyOrNullDateString() throws Exception {
		checkDateStrThrowsException(null);
		checkDateStrThrowsException("    ");
	}

	private void checkDateStrThrowsException(String dateStr) throws ParseException {
		try {
			SmartDateParser.parseDate(dateStr);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			assertIsNotBlankException("dateStr", ex);
		}
	}

	public void testParseDateMMDDYYYY() throws Exception {
		parseDate("09-07-1997");
		parseDate("09-7-1997");
		parseDate("9-7-1997");
		parseDate("09/7/1997");
		parseDateBigDay("13-09-1997");
	}

	public void testParseDateDDMMMYYYY() throws Exception {
		parseDate("07-Sep-1997");
		parseDate("7-Sep-1997");
		parseDate("07-SEP-1997");
		parseDate("07-SEP-97");
	}

	public void testParseDateYYMMMDD() throws Exception {
		parseDate("97-SEP-07");
		parseDate("97-SEP-7");
	}

	public void testParseDateDDMMMMMYYYY() throws Exception {
		parseDate("7-September-1997");
		parseDate("7 September 1997");
	}

	public void testParseDateYYYYMMDD() throws Exception {
		parseDate("1997-09-7");
		parseDate("1997-9-7");
		parseDate("1997. 09. 7.");
		parseDate("1997. 9. 7.");
	}

	public void testParseDateYYYYMDD() throws Exception {
		parseDate("1997-9-7");
		parseDate("1997/9/7");
	}

	public void testParseDateYYYYMMDDNoDashes() throws Exception {
		parseDate("19970907");
	}

	public void testParseDateYYYYMMDDTHHMMSSZ() throws Exception {
		parseDateTime("1997-09-07T05:03:57Z");
	}

	public void testParseDateYYYYMMDDHHMM() throws Exception {
		parseDateTimeNoSeconds("1997-09-07 05:03");
	}

	public void testParseDateDDMMMYYYYHHMMSS() throws Exception {
		parseDateTime("07 Sep 1997 05:03:57");
		parseDateTime("07 Sep 1997 5:03:57");
		parseDateTime("07 September 1997 05:03:57");
		parseDateTime("07 September 1997 5:03:57");
	}

	public void testParseDateDDMMMYYYYHHMMSSZ() throws Exception {
		parseDateTime("07-Sep-1997 01:03:57 AST");
		parseDateTime("07-Sep-1997 1:03:57 AST");
		parseDateTime("7-Sep-1997 00:03:57 EST");
		parseDateTime("07-September-1997 00:03:57 EST");
	}

	public void testParseDateEEEMMMDDYYYY() throws Exception {
		parseDate("Sun, Sep 7, 1997");
		parseDate("Sun Sep 7 1997");
		parseDate("Sun Sep  7 1997");
	}

	public void testParseDateEEEDDthMMMYYYY() throws Exception {
		parseDate("Sunday 7th September 1997");
	}

	public void testParseDateEEEDDstMMMYYYY() throws Exception {
		Date actual = SmartDateParser.parseDate("Monday 1st September 1997");
		assertDateEquals(1997, 9, 1, 0, 0, 0, "UTC", actual);
	}

	public void testParseDateEEEDDndMMMYYYY() throws Exception {
		Date actual = SmartDateParser.parseDate("Tuesday 2nd September 1997");
		assertDateEquals(1997, 9, 2, 0, 0, 0, "UTC", actual);
	}

	public void testParseDateEEEDDrdMMMYYYY() throws Exception {
		Date actual = SmartDateParser.parseDate("Wednesday 3rd September 1997");
		assertDateEquals(1997, 9, 3, 0, 0, 0, "UTC", actual);
	}

	public void testParseDateEEEDDMMMYYYYHHMMSSZ() throws Exception {
		parseDateTime("Sun, 7 Sep 1997 00:03:57 EST");
		parseDateTime("Sunday, 7 Sep 1997 00:03:57 EST");
		parseDateTime("Sun, 7 September 1997 00:03:57 EST");
		parseDateTime("Sunday, 7 September 1997 00:03:57 EST");
	}

	public void testParseDateYYYYMMDDHHMMSSS() throws Exception {
		parseDateTimeMillis("1997-09-07 05:03:57.291");
		parseDateTime("1997-09-07 05:03:57");
		parseDateTime("1997-09-07 00:03:57 EST");
	}

	public void testParseDateMMDDYYYYHHMMSSS() throws Exception {
		parseDateTime("09/7/1997 05:03:57");
	}

	public void testParseDateEEEMMMDDHHMMSSZYYYY() throws Exception {
		parseDateTime("Sun Sep 7 00:03:57 EST 1997");
		parseDateTime("Sun Sep 7 00:3:57 EST 1997");
		parseDateTime("Sun Sep 7 0:03:57 EST 1997");
		parseDateTime("Sun Sep 7 0:3:57 EST 1997");
	}

	public void testParseDateYYMMMDDThrowsParseException() throws Exception {
		checkParseDateThrowsParseException("03-SEP-09");
		checkParseDateThrowsParseException("31-SEP-09");
		checkParseDateThrowsParseException("03-SEP-31");
		checkParseDateThrowsParseException("1997/march/2");
		checkParseDateThrowsParseException("1997907");
	}

	private void checkParseDateThrowsParseException(String dateStr) {
		try {
			SmartDateParser.parseDate(dateStr);
			fail();
		} catch (ParseException ex) {
			// expected case
			assertEquals("Unsupported date format: " + dateStr, ex.getMessage());
		}
	}

	private void parseDate(String dateStr) throws ParseException {
		Date actual = SmartDateParser.parseDate(dateStr);
		assertNotNull("No pattern found for " + dateStr, actual);
		assertDateEquals(1997, 9, 7, 0, 0, 0, "UTC", actual);
	}

	private void parseDateBigDay(String dateStr) throws ParseException {
		Date actual = SmartDateParser.parseDate(dateStr);
		assertNotNull("No pattern found for " + dateStr, actual);
		assertDateEquals(1997, 9, 13, 0, 0, 0, "UTC", actual);
	}

	private void parseDateTime(String dateStr) throws ParseException {
		Date actual = SmartDateParser.parseDate(dateStr);
		assertNotNull("No pattern found for " + dateStr, actual);
		assertDateEquals(1997, 9, 7, 5, 3, 57, "UTC", actual);
	}

	private void parseDateTimeNoSeconds(String dateStr) throws ParseException {
		Date actual = SmartDateParser.parseDate(dateStr);
		assertNotNull("No pattern found for " + dateStr, actual);
		assertDateEquals(1997, 9, 7, 5, 3, 0, "UTC", actual);
	}

	private void parseDateTimeMillis(String dateStr) throws ParseException {
		Date actual = SmartDateParser.parseDate(dateStr);
		assertNotNull("No pattern found for " + dateStr, actual);
		assertDateEquals(1997, 9, 7, 5, 3, 57, 291, "UTC", actual);
	}

	// ------------------------------------- parseDateAsLong

	private static final long EXP_TIME = makeTimeInMillis(1997, 9, 7, 0, 0, 0, 0, "UTC");

	public void testParseDateAsLongReturnsTimestampsAsLong() {
		assertEquals(EXP_TIME, SmartDateParser.parseDateAsLong(String.valueOf(EXP_TIME)));
		assertEquals(EXP_TIME, SmartDateParser.parseDateAsLong(String.valueOf(EXP_TIME / DateUtils.MILLIS_PER_SECOND)));
	}

	public void testParseDateAsLongReturnsParsedDateAsLong() {
		assertEquals(EXP_TIME, SmartDateParser.parseDateAsLong("09-07-1997"));
	}

	public void testParseDateReturnsUnknownTimeInEventOfUnsupportedDateFormat() {
		long res = parseDateAsLongUsingLog(new NoErrorLog(), "03-SEP-09");
		assertEquals(SmartDateParser.UNKNOWN_TIME, res);
	}

	public void testParseDateLogsErrorInEventOfUnsupportedDateFormat() {
		NoErrorLog log = new NoErrorLog();
		parseDateAsLongUsingLog(log, "03-SEP-09");
		assertEquals("Unparseable date: 03-SEP-09", log.errMsgArg);
		assertEquals(ParseException.class, log.exArg.getClass());
		assertEquals("Unsupported date format: 03-SEP-09", log.exArg.getMessage());
	}

	private long parseDateAsLongUsingLog(Log log, String dateTimeStr) {
		Log logOrig = SmartDateParser.log;
		SmartDateParser.log = log;
		try {
			return SmartDateParser.parseDateAsLong(dateTimeStr);
		} finally {
			SmartDateParser.log = logOrig;
		}
	}

	@SuppressWarnings("serial")
	public static class NoErrorLog extends NoOpLog {
		public Object errMsgArg;
		public Throwable exArg;

		@Override
		public void error(Object msg, Throwable ex) {
			errMsgArg = msg;
			exArg = ex;
		}
	}
}
