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

import java.io.File;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import com.fraudwall.util.exc.AnchorFatalError;

/**
 * Defines various utility methods useful for parsing, formatting,
 * and manipulating dates and/or times.
 */
public final class DateTimeUtils {
	public static final long NULL_TIME = -1;
	public static final String SQL_NULL = "NULL";

	public static final long SECONDS_PER_MINUTE = DateUtils.MILLIS_PER_MINUTE / DateUtils.MILLIS_PER_SECOND;
	public static final long SECONDS_PER_HOUR = DateUtils.MILLIS_PER_HOUR / DateUtils.MILLIS_PER_SECOND;
	public static final long SECONDS_PER_DAY = DateUtils.MILLIS_PER_DAY / DateUtils.MILLIS_PER_SECOND;

	private DateTimeUtils() {
	}

	public static final DateFormat ANCHOR_LOG_FILENAME_DATE_FORMAT =
		new AnchorDateFormat("yyyy-MM-dd-HH-mm-ss");

	private static final DateFormat APACHE_DATE_FORMAT =
		new AnchorDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
	private static final Pattern APACHE_PATTERN =
		Pattern.compile("\\d{2}/[a-zA-Z]{3}/\\d{4}:\\d{2}:\\d{2}:\\d{2} [+-]\\d{4}");

	/**
	 * Parses a date/time string in Apache log format, and returns the result as
	 * a Date.
	 *
	 * @param ts
	 *            A date/time in the form "dd/Mon/yyyy:hh:mm:ss TZ" where "dd"
	 *            is the 2-digit day of the month (1-based), "Mon" is the
	 *            3-letter month name, "yyyy" is the 4-digit year, "hh" is the
	 *            hour of the day (in 24-hour time), "mm" is the minute of the
	 *            hour, "ss" is the second of the minute, and "TZ" is a RFC 822
	 *            time-zone specifier (see {@link SimpleDateFormat} for details.
	 *            For example, "12/Aug/2003:23:34:45 -0800" is August 12, 2003,
	 *            at 11:34 pm and 45 seconds at -8 hours relative to GMT (i.e.,
	 *            Pacific standard time).
	 * @throws ParseException
	 *             If <code>ts</code> is not in the form required above.
	 * @see SimpleDateFormat
	 */
	public static Date dateFromApacheTimestamp(String ts) throws ParseException {
		if (!APACHE_PATTERN.matcher(ts).matches()) {
			throw new ParseException("Unparseable date: \"" + ts + "\"", 0);
		}
		synchronized (APACHE_DATE_FORMAT) {
			Date d = APACHE_DATE_FORMAT.parse(ts);
			return d;
		}
	}

	private static final DateFormat IIS_DATE_FORMAT =
		new AnchorDateFormat("yyyy-MM-dd HH:mm:ss Z");

	private static final Pattern IIS_DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
	private static final Pattern IIS_TIME_PATTERN = Pattern.compile("\\d{2}:\\d{2}:\\d{2}");

	/**
	 * Parses date/time strings in IIS log format, and returns the result as a
	 * Date.
	 *
	 * @param date
	 *            A date in the form "yyyy-mm-dd", where "yyyy" is the 4-digit
	 *            year, "mm" is the 2-digit month (1-based), and "dd" is the
	 *            2-digit day of the month (also 1-based). For example,
	 *            "2003-08-12" denotes August 12, 2003.
	 * @param time
	 *            A time in the form "hh:mm:ss", where "hh" is the hour of the
	 *            day (in 24-hour time), "mm" is the minute of the hour, "ss" is
	 *            the second of the minute. All times are interpreted in UTC
	 *            (i.e., GMT) For example, "23:34:45" is 11:34 pm and 45 seconds
	 *            at GMT (or 3:34pm and 45 seconds Pacific standard time).
	 * @throws ParseException
	 *             If either <code>date</code> or <code>time</code> is not
	 *             in the form required above.
	 * @see SimpleDateFormat
	 */
	public static Date dateFromIISTimestamp(String date, String time)
			throws ParseException {
		// IIS is reported with GMT time zone
		String composite = date + " " + time + " -0000";
		if (!IIS_DATE_PATTERN.matcher(date).matches() || !IIS_TIME_PATTERN.matcher(time).matches()) {
			throw new ParseException("Unparseable date: \"" + composite + "\"", 0);
		}
		return dateFromIISTimestamp(composite);
	}

	/**
	 * Parses date/time strings in IIS log format, and returns the result as a
	 * Date.
	 *
	 * @param datetime
	 *            A date in the form "yyyy-mm-dd hh:mm:ss Z", where "yyyy" is the 4-digit
	 *            year, "mm" is the 2-digit month (1-based), "dd" is the
	 *            2-digit day of the month (also 1-based), "hh" is the hour of the
	 *            day (in 24-hour time), "mm" is the minute of the hour, "ss" is
	 *            the second of the minute, and Z is the time offset from GMT. For example,
	 *            "2003-08-12 3:34pm -0000" denotes August 12, 2003 3:34 pm at GMT
	 *            (or 3:34pm and 45 seconds Pacific standard time).
	 * @throws ParseException
	 *             If <code>datetime</code> is not in the form required above.
	 * @see SimpleDateFormat
	 */
	public static Date dateFromIISTimestamp(String datetime) throws ParseException {
		synchronized (IIS_DATE_FORMAT) {
			Date d = IIS_DATE_FORMAT.parse(datetime);
			return d;
		}
	}

	private static final ThreadSafeDateFormat UTC_DATE_TIME_FORMAT =
		new ThreadSafeDateFormat("yyyy-MM-dd HH:mm:ss", "UTC");
	private static final ThreadSafeDateFormat UTC_DATE_FORMAT =
		new ThreadSafeDateFormat("yyyy-MM-dd", "UTC");

	/**
	 * Formats a date/time specified as the number of milliseconds since the
	 * epoch as a date/time string in the form "YYYY-MM-DD hh:mm:ss", where
	 * "YYYY" is the 4-digit year, "MM" is the 2-digit month (1-based), "DD" is
	 * the 2-digit day of the month (1-based), "hh" is the 2-digit hour of the
	 * day (in 24-hour time), "mm" is the 2-digit minute of the hour, and "ss"
	 * is the two-digit second of the minute. As the method name implies, the
	 * resulting date/time string is in UTC time.
	 *
	 * @param utcMillis
	 *            The number of milliseconds since the epoch.
	 * @return The date/time of the argument in UTC time.
	 */
	public static String utcDateTimeFromMillis(long utcMillis) {
		return utcMillis <= 0 ? null : UTC_DATE_TIME_FORMAT.format(new Date(utcMillis));
	}

	public static String utcDateTimeFromMillisForCsv(long utcMillis) {
		return sqlNullify(utcDateTimeFromMillis(utcMillis));
	}

	/**
	 * Returns the number of millis since the epoch represented by the date/time
	 * string in the form "YYYY-MM-DD hh:mm:ss", where
	 * "YYYY" is the 4-digit year, "MM" is the 2-digit month (1-based), "DD" is
	 * the 2-digit day of the month (1-based), "hh" is the 2-digit hour of the
	 * day (in 24-hour time), "mm" is the 2-digit minute of the hour, and "ss"
	 * is the two-digit second of the minute.
	 *
	 * @param utcDate
	 *            The date formatted as string
	 *
	 * @return the number of milliseconds since the epoch
	 */
	public static long utcDateTimeToMillis(String utcDate) {
		if (StringUtils.isBlank(utcDate) || SQL_NULL.equals(utcDate)) {
			return NULL_TIME;
		}
		try {
			return UTC_DATE_TIME_FORMAT.parse(utcDate).getTime();
		} catch (ParseException e) {
			throw new AnchorFatalError("Unable to parse Date " + utcDate, e);
		}
	}

	/**
	 * Formats a date specified as the number of milliseconds since the epoch as
	 * a date string in the form "YYYY-MM-DD", where "YYYY" is the 4-digit year,
	 * "MM" is the 2-digit month (1-based), and "DD" is the 2-digit day of the
	 * month (1-based). As the method name implies, the resulting date/time
	 * string is in UTC time.
	 *
	 * @param utcMillis
	 *            The number of milliseconds since the epoch.
	 * @return The date/time of the argument in UTC time or null if utcMillis is
	 *         negative.
	 */
	public static String utcDateFromMillis(long utcMillis) {
		return utcMillis < 0 ? null : UTC_DATE_FORMAT.format(new Date(utcMillis));
	}

	public static String utcDateFromMillisForCsv(long utcMillis) {
		return sqlNullify(utcDateFromMillis(utcMillis));
	}

	private static String sqlNullify(String s) {
		return s == null ? SQL_NULL : s;
	}
	/**
	 * Returns the number of millis since the epoch represented by the date
	 * string in the form "YYYY-MM-DD", where "YYYY" is the 4-digit year, "MM"
	 * is the 2-digit month (1-based), and "DD" is the 2-digit day of the month
	 * (1-based).
	 *
	 * @param utcDate
	 *            The date formatted as string
	 *
	 * @return the number of milliseconds since the epoch or {@link #NULL_TIME}
	 *         if utcDate is null
	 */
	public static long utcDateToMillis(String utcDate) {
		if (StringUtils.isBlank(utcDate) || SQL_NULL.equals(utcDate)) {
			return NULL_TIME;
		}
		try {
			return UTC_DATE_FORMAT.parse(utcDate).getTime();
		} catch (ParseException e) {
			throw new AnchorFatalError("Unable to parse Date " + utcDate, e);
		}
	}

	/**
	 * Returns the Timestamp corresponding to <code>millis</code>, which denotes
	 * the point in time that many milliseconds since the epoch; returns <code>null</code>
	 * if <code>millis</code> is negative.
	 */
	public static Timestamp toTimestampFromLong(long millis) {
		if (millis < 0) {
			return null;
		}
		return new Timestamp(millis);
	}

	/**
	 * Returns the number of milliseconds since the epoch represented
	 * by the time stamp <code>ts</code>, or {@link #NULL_TIME} if
	 * <code>ts</code> is <code>null</code>.
	 */
	public static long longFromTimestamp(Timestamp ts) {
		if (ts == null) {
			return NULL_TIME;
		}
		return ts.getTime();
	}

	/**
	 * Converts the duration <code>seconds</code> to milliseconds, returning
	 * the result; returns {@link #NULL_TIME} if <code>seconds</code> is
	 * negative.
	 */
	public static long durationFromSQL(int seconds) {
		if (seconds < 0) {
			return NULL_TIME;
		}
		return seconds * DateUtils.MILLIS_PER_SECOND;
	}

	/**
	 * Converts the duration <code>millis</code> to seconds, returning
	 * the result; returns <code>millis</code> itself if it is negative.
	 */
	public static int durationToSQL(long millis) {
		if (millis < 0) {
			return (int) millis;
		}
		return (int) (millis / DateUtils.MILLIS_PER_SECOND);
	}

	/**
	 * Returns the duration in milliseconds of the String <code>seconds</code>,
	 * which is a duration in seconds. Returns {@link #NULL_TIME} if
	 * <code>seconds</code> is <code>null</code>.
	 *
	 * @see #durationToString
	 */
	public static long durationFromString(String seconds) {
		if (seconds == null) {
			return NULL_TIME;
		}
		int numSeconds = Integer.parseInt(seconds);
		return numSeconds < 0 ? NULL_TIME : numSeconds * DateUtils.MILLIS_PER_SECOND;
	}

	/**
	 * Returns a String representation of the number of whole seconds in
	 * <code>millis</code>, a duration in milliseconds. Any fractional second
	 * is truncated.
	 *
	 * @see #durationFromString
	 */
	public static String durationToString(long millis) {
		return String.valueOf(millis < 0 ? NULL_TIME : millis / DateUtils.MILLIS_PER_SECOND);
	}

	/**
	 * Returns the date boundary Date for the given <code>date</code> that
	 * has the hour/minute/second/millisecond zeroed out. Analogous to the MYSQL
	 * DATE() function.
	 */
	public static Date dateFloor(Date date) {
		if (date == null) {
			return null;
		}
		long timestamp = date.getTime();
		return new Date(dateFloor(timestamp));
	}

	/**
	 * Returns the date boundary timestamp(long) for the given <code>date</code> that
	 * has the hour/minute/second/millisecond zeroed out. Analogous to the MYSQL
	 * DATE() function.
	 */
	public static long dateFloor(long timestamp) {
		return ((int) (timestamp / DateUtils.MILLIS_PER_DAY)) * DateUtils.MILLIS_PER_DAY;
	}

	/**
	 * Returns the date embedded in the given file name as milliseconds since
	 * the epoch. Returns zero if the file does not match the given
	 * {@code fileNamePattern}.
	 *
	 * @param fileNamePattern
	 *            The pattern that the file must match. It's first group should
	 *            be the date portion of the file name.
	 * @param dateFormat
	 *            The format used to parse the date portion of the file name.
	 */
	public static long dateFromFilename(File file, Pattern fileNamePattern, DateFormat dateFormat) {
		long utc = 0;
		Matcher m = fileNamePattern.matcher(file.getName());
		if (m.matches()) {
			utc = dateFormat.parse(m.group(1), new ParsePosition(0)).getTime();
		}
		return utc;
	}
}
