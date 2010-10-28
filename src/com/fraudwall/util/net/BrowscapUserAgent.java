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
/*
 * Copyright (c) 2007, Fraudwall Technologies. All rights reserved.
 */

package com.fraudwall.util.net;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fraudwall.util.FWProps;

/**
 * Encapsulates a single entry in a browser capability file. This consists of a
 * user agent pattern (a regular expression string containing "?" and "*"
 * wildcard characters), the line number in which the pattern appeared in the
 * file, and zero or more key=value pairs.
 */
@SuppressWarnings("serial")
public class BrowscapUserAgent extends HashMap<String, String>
	implements Comparable<BrowscapUserAgent>
{
	private static final String ACCEPT_SEMI_COLON_FOR_COMMA_PROP_NAME = "useragent.browscap.acceptSemiColonForComma";
	private static final String FALSE_VALUE = "0";
	private static final String TRUE_VALUE = "1";

	/** The user agent name pattern (may contain "?" and "*" characters. */
	private final String name;

	/** The line number in which the pattern appears in the browscap file. */
	private final int lineNumber;

	/** The compiled form of the "name" pattern. */
	private final Pattern pattern;

	public BrowscapUserAgent(String name, int lineNumber) {
		this.name = name;
		this.lineNumber = lineNumber;
		pattern = browscapPatternToJavaPattern(name);
	}

	@Override
	public String put(String key, String value) {
		if ("true".equals(value)) {
			value = TRUE_VALUE;
		} else if ("false".equals(value)) {
			value = FALSE_VALUE;
		}
		return super.put(key, value);
	}

	/**
	 * Returns true if and only if this BrowscapUserAgent has a key
	 * named <code>key</code> set to the value "true" in the input
	 * browscap file.
	 */
	public boolean getBooleanValue(String key) {
		String value = get(key);
		if (value == null || FALSE_VALUE.equals(value)) {
			return false;
		} else if (TRUE_VALUE.equals(value)) {
			return true;
		}
		throw new IllegalArgumentException(
			"Value for key \"" + key + "\" is \"" + value +
			"\", which is neither " + FALSE_VALUE + " nor " + TRUE_VALUE);
	}

	public boolean matches(String s) {
		Matcher m = pattern.matcher(s);
		return m.matches();
	}

	/**
	 * Sorts browscap entries based on the length of their patterns
	 * by descreasing length. Ties amongst patterns of equal length
	 * are broken by sorting the patterns lexicographically.
	 */
	public int compareTo(BrowscapUserAgent ua) {
		int diff = ua.name.length() - name.length();
		if (diff != 0) {
			// sort longest strings first
			return diff;
		} else {
			return name.compareTo(ua.name);
		}
	}

	public boolean equals(BrowscapUserAgent ua) {
		return this.compareTo(ua) == 0;
	}

	public String getName() {
		return name;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	@Override
	public String toString() {
		return "[NAME={" + name + "},attrs=" + super.toString() + "]";
	}

	/**
	 * Converts a browscap regular expression that may contain "?" and
	 * "*" wildcards to a compiled Java regular expression. This just
	 * maps "?" to "." and "*" to ".*", and escapes the other regular
	 * expression meta-characters ".", "+", "$", "\", "|", "(", ")",
	 * "{", "}", "[", and "]".
	 *
	 * @return The compiled regular expression as a Java {@link Pattern}.
	 */
	private static Pattern browscapPatternToJavaPattern(String s) {
		boolean acceptSemiColonForComma = FWProps.getBooleanProperty(ACCEPT_SEMI_COLON_FOR_COMMA_PROP_NAME);
		StringBuilder sb = new StringBuilder((s.length() * 2) + 2);
		sb.append('^');
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '?':
				sb.append('.');
				break;
			case '*':
				sb.append(".*");
				break;
			case '.':
			case '+':
			case '$':
			case '\\':
			case '|':
			case '(':
			case ')':
			case '{':
			case '}':
			case '[':
			case ']':
				sb.append('\\').append(c);
				break;
			case ',':
				if (acceptSemiColonForComma) {
					sb.append("[,;]");
				} else {
					sb.append(c);
				}
				break;
			default:
				sb.append(c);
				break;
			}
		}
		sb.append('$');
		return Pattern.compile(sb.toString());
	}
}

/*
 * Local variables:
 * tab-width: 4
 * c-basic-offset: 4
 * End:
 * vim600: noet sw=4 ts=4 fdm=marker
 * vim<600: noet sw=4 ts=4
 */
