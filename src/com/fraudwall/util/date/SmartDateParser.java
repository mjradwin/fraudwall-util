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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fraudwall.util.exc.AnchorFatalError;
import com.fraudwall.util.exc.ArgCheck;

/**
 * Parses date and date/time strings in a variety of common formats.
 * @author kfox
 */
public class SmartDateParser {

	public static Log log = LogFactory.getLog(SmartDateParser.class);

	public static final long UNKNOWN_TIME = 0L;

	private static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("UTC");

	// This needs to be a linked hash map so that the specification order is the iteration order.  Otherwise,
	// we have to be very careful that there is no pattern overlap.  Also this way we get consistent behavior.
	private static final Map<Pattern,ThreadSafeDateFormat> DATE_FORMATTERS = new LinkedHashMap<Pattern,ThreadSafeDateFormat>();
	static {
		// time in seconds/milliseconds
		addDateFormat("\\d{8}\\d+", null);
		// 15-09-1997
		addDateFormat("(1[3-9]|2[0-9]|3[0-1])-\\d+-\\d{4}", "dd-MM-yyyy");
		// 09-15-1997
		addDateFormat("\\d+-\\d+-\\d{4}", "MM-dd-yyyy");
		// 09/15/1997
		addDateFormat("\\d+/\\d+/\\d{4}", "MM/dd/yyyy");
		// 1997-09-15
		addDateFormat("\\d{4}-\\d?\\d-\\d?\\d", "yyyy-MM-dd");
		// 1997-09-15 00:00
		addDateFormat("\\d{4}-\\d?\\d-\\d?\\d \\d+:\\d+", "yyyy-MM-dd HH:mm");
		// 2009/7/17
		addDateFormat("\\d{4}/\\d?\\d/\\d?\\d", "yyyy/MM/dd");
		// 19970915
		addDateFormat("\\d{8}", "yyyyMMdd");
		// 15-Sep-1997
		addDateFormat("\\d+-[A-Za-z]+-\\d{4}", "dd-MMM-yyyy");
		// 15 Sep 1997
		addDateFormat("\\d+ [A-Za-z]+ \\d{4}", "dd MMM yyyy");
		// 16-SEP-97
		addDateFormat("\\d{1,2}-[A-Za-z]+-[4-9]\\d", "dd-MMM-yy");
		addDateFormat("\\d{1,2}-[A-Za-z]+-3[2-9]", "dd-MMM-yy");
		// 97-SEP-16
		addDateFormat("[4-9]\\d-[A-Za-z]+-\\d{1,2}", "yy-MMM-dd");
		addDateFormat("3[2-9]-[A-Za-z]+-\\d{1,2}", "yy-MMM-dd");
		// 1997. 09. 16
		addDateFormat("\\d{4}. \\d{1,2}. \\d?\\d.", "yyyy. MM. dd");
		// 1997-09-15T00:00:00Z
		addDateFormat("\\d{4}-\\d+-\\d+T\\d{1,2}:\\d{1,2}:\\d{1,2}Z", "yyyy-MM-dd'T'HH:mm:ss'Z'");
		// 26 Mar 2009 04:00:00
		addDateFormat("\\d{1,2} [A-Za-z]+ \\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}", "dd MMM yyyy HH:mm:ss");
		// 17-Feb-2009 16:59:21 EST
		addDateFormat("\\d+-[A-Za-z]+-\\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2} [A-Za-z]{3}", "dd-MMM-yyyy HH:mm:ss Z");
		// Sun Mar 01 2009
		addDateFormat("[A-Za-z]+ [A-Za-z]+ [0-9 ]?\\d \\d{4}", "EEE MMM dd yyyy");
		// Tuesday 7th September 1997
		addDateFormat("[A-Za-z]+ [0-9]+th+ [A-Za-z]+ \\d{4}", "EEE dd'th' MMM yyyy");
		// Tuesday 1st September 1997
		addDateFormat("[A-Za-z]+ [0-9]+st+ [A-Za-z]+ \\d{4}", "EEE dd'st' MMM yyyy");
		// Tuesday 2nd September 1997
		addDateFormat("[A-Za-z]+ [0-9]+nd+ [A-Za-z]+ \\d{4}", "EEE dd'nd' MMM yyyy");
		// Tuesday 3rd September 1997
		addDateFormat("[A-Za-z]+ [0-9]+rd+ [A-Za-z]+ \\d{4}", "EEE dd'rd' MMM yyyy");
		// Sun, Mar 01, 2009
		addDateFormat("[A-Za-z]+, [A-Za-z]+ \\d?\\d, \\d{4}", "EEE, MMM dd, yyyy");
		// Tue, 17 Feb 2009 19:00:31 EST
		addDateFormat("[A-Za-z]+, \\d?\\d [A-Za-z]+ \\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2} [A-Za-z]{3}",
					"EEE, dd MMM yyyy HH:mm:ss Z");
		// Tue Jan 12 23:59:59 GMT 2010
		addDateFormat("[A-Za-z]+ [A-Za-z]+ \\d?\\d \\d{1,2}:\\d{1,2}:\\d{1,2} [A-Za-z]{3} \\d{4}",
					"EEE MMM dd HH:mm:ss Z yyyy");
		// 2017-04-14 18:36:57 UTC
		addDateFormat("\\d{4}-\\d?\\d-\\d?\\d \\d{1,2}:\\d{1,2}:\\d{1,2} [A-Za-z]{3}", "yyyy-MM-dd HH:mm:ss Z");
		// 2017-04-14 18:36:57
		addDateFormat("\\d{4}-\\d?\\d-\\d?\\d \\d{1,2}:\\d{1,2}:\\d{1,2}", "yyyy-MM-dd HH:mm:ss");
		// 04/14/2017 18:36:57
		addDateFormat("\\d{1,2}/\\d?\\d/\\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}", "MM/dd/yyyy HH:mm:ss");
		// 2017-04-14 18:36:57.0
		addDateFormat("\\d{4}-\\d?\\d-\\d?\\d \\d{1,2}:\\d{1,2}:\\d{1,2}.\\d+", "yyyy-MM-dd HH:mm:ss.S");
	}

	private static void addDateFormat(String regex, String pattern) {
		ArgCheck.isNotNull(regex, "regex");
		DATE_FORMATTERS.put(Pattern.compile(regex),
			pattern == null ? null : new ThreadSafeDateFormat(pattern, UTC_TIMEZONE));
	}

	public static long parseDateToMillis(String dateStr) {
		if (StringUtils.isBlank(dateStr) || DateTimeUtils.SQL_NULL.equals(dateStr)) {
			return DateTimeUtils.NULL_TIME;
		}
		try {
			return parseDate(dateStr).getTime();
		} catch (ParseException e) {
			throw new AnchorFatalError("Unable to parse Date " + dateStr, e);
		}
	}

	/**
	 * Tries to figure out which {@link DateFormat} to use by using regex on the
	 * date string. Throws a ParseException if it is unable to recognize the
	 * date format. Expects a non empty string as a parameter
	 */
	public static Date parseDate(String dateStr) throws ParseException {
		ArgCheck.isNotBlank(dateStr, "dateStr");
		for (Pattern pattern : DATE_FORMATTERS.keySet()) {
			Matcher matcher = pattern.matcher(dateStr);
			if (matcher.matches()) {
				ThreadSafeDateFormat dateFormat = DATE_FORMATTERS.get(pattern);
				if (log.isDebugEnabled()) {
					log.debug("date: " + dateStr + " format: " + dateFormat.getDateFormatString());
				}
				if (dateFormat == null) {
					long timestamp = Long.parseLong(dateStr);
					if (timestamp < DateUtils.MILLIS_PER_DAY * 365) {
						// assume the time is in seconds
						timestamp *= DateUtils.MILLIS_PER_SECOND;
					}
					return new Date(timestamp);
				} else {
					return dateFormat.parse(dateStr);
				}
			}
		}
		throw new ParseException("Unsupported date format: " + dateStr, -1);
	}

	/**
	 * Like {@link #parseDate(String)}, but converts the result to a long value
	 * using {@link Date#getTime()}, and returns {@link #UNKNOWN_TIME} in the
	 * event of a {@link ParseException}.
	 */
	public static long parseDateAsLong(String dateStr) {
		try {
			return parseDate(dateStr).getTime();
		} catch (ParseException ex) {
			log.error("Unparseable date: " + dateStr, ex);
			return UNKNOWN_TIME;
		}
	}
}
