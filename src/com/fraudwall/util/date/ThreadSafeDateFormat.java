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
import java.util.TimeZone;


/**
 * Wrapper for an {@link AnchorDateFormat} whose parse and format methods are
 * all synchronized for thread safety.
 */
public class ThreadSafeDateFormat {
	private final String format;
	private final DateFormat df;

	public ThreadSafeDateFormat(String format, String timeZoneName) {
		this(format, TimeZone.getTimeZone(timeZoneName));
	}

	public ThreadSafeDateFormat(String format, TimeZone tz) {
		this.format = format;
		df = new AnchorDateFormat(format, tz);
	}

	public ThreadSafeDateFormat(DateFormat df) {
		this.df = (DateFormat)df.clone();
		this.format = null;
	}

	public synchronized Date parse(String s) throws ParseException {
		return df.parse(s);
	}

	public synchronized String format(Date date) {
		return df.format(date);
	}

	public synchronized String format(long date) {
		return df.format(date);
	}

	public synchronized DateFormat getDateFormat() {
		return (DateFormat)df.clone();
	}

	public String getDateFormatString() {
		return format;
	}
}
