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

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.fraudwall.util.ArgCheck;

/**
 * Simple sub-class of {@link SimpleDateFormat} that always does strict
 * date parsing.
 *
 * @see SimpleDateFormat#setTimeZone(TimeZone)
 * @see SimpleDateFormat#setLenient(boolean)
 * @see ThreadSafeDateFormat
 *
 * @author Allan Heydon
 */
public class AnchorDateFormat extends SimpleDateFormat {
	private static final long serialVersionUID = 1427550539239553107L;

	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

	/**
	 * Constructs a new date formatter using the given <code>pattern</code>.
	 * By default, this instance will interpret dates in the UTC time zone
	 * and will <em>not</em> use lenient mode when parsing dates.
	 */
	public AnchorDateFormat(String pattern) {
		this(pattern, UTC);
	}

	/**
	 * Constructs a new date formatter using the given <code>pattern</code>,
	 * interpreting dates in the time zone named <code>timeZoneName</code>.
	 * By default, this instance will <em>not</em> use lenient mode when parsing dates.
	 */
	public AnchorDateFormat(String pattern, String timeZoneName) {
		this(pattern, TimeZone.getTimeZone(timeZoneName));
	}

	/**
	 * Constructs a new date formatter using the given <code>pattern</code>,
	 * interpreting dates in the time zone <code>tz</code>. By default, this
	 * instance will <em>not</em> use lenient mode when parsing dates.
	 *
	 * @see SimpleDateFormat#setLenient(boolean)
	 */
	public AnchorDateFormat(String pattern, TimeZone tz) {
		super(pattern);
		ArgCheck.isNotNull(pattern, "pattern");
		ArgCheck.isNotNull(tz, "tz");
		setTimeZone(tz);
		setLenient(false);
	}
}
