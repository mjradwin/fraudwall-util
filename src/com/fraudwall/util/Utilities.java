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
package com.fraudwall.util;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import com.fraudwall.util.exc.AnchorFatalError;
import com.fraudwall.util.exc.ArgCheck;
import com.fraudwall.util.exc.Require;


/**
 * A hodge-podge of general-purpose utilities functions.
 */
public final class Utilities {

	private static final String OS = System.getProperty("os.name");
	private static String host = null;

	private Utilities() {
	}

	/**
	 * Returns the user name found in the environment variable "FWUSER" if it is
	 * non-empty, or the value of the system property "user.name" otherwise. The
	 * latter corresponds to the actual user ID that owns the process.
	 */
	public static String getCurrentUser() {
		String user = System.getenv("FWUSER");
		if (StringUtils.isEmpty(user)) {
			user = System.getProperty("user.name");
		}
		return user;
	}

	/**
	 * Returns the non-fully qualified host name for this system.
	 * So, "mithra" instead of "mithra.fraudwall.net"
	 *
	 * Throws AnchorFatalError in the event of that the hostname
	 * can not be found
	 */
	public static String getCurrentHost() {
		if (host == null) {
			try {
				host = firstComponent(InetAddress.getLocalHost().getHostName());
			} catch (UnknownHostException e) {
				throw new AnchorFatalError("Unable to determine hostname", e);
			}
		}
		return host;
	}

	/**
	 * Returns a list of all IP addresses that this host is configured
	 * to listen on.
	 */
	public static List<InetAddress> getInetAddresses() {
		try {
			List<InetAddress> addrList = new ArrayList<InetAddress>();
			Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
			while (ifaces.hasMoreElements()) {
				NetworkInterface iface = ifaces.nextElement();
				Enumeration<InetAddress> addrs = iface.getInetAddresses();
				while(addrs.hasMoreElements()) {
					InetAddress addr = addrs.nextElement();
					addrList.add(addr);
				}
			}
			return addrList;
		} catch (SocketException e) {
			throw new AnchorFatalError("Unable to determine IP addresses", e);
		}
	}

	/**
	 * Returns true iff the name of the current host is <code>hostName</code>
	 * or any of the IP addresses which the current host resolves to
	 * match the IP address of <code>hostName</code>.
	 */
	public static boolean isCurrentHost(String hostName) {
		if (getCurrentHost().equals(hostName)) {
			return true;
		}
		List<InetAddress> inetAddresses = Utilities.getInetAddresses();
		try {
			for (InetAddress addr : InetAddress.getAllByName(hostName)) {
				if (inetAddresses.contains(addr)) {
					return true;
				}
			}
			return false;
		} catch (UnknownHostException e) {
			throw new AnchorFatalError("Unable to determine IP addresses of host " + hostName, e);
		}
	}

	/**
	 * Determine if this host is a desktop or a server
	 * by looking at the OS.  If this host is linux, then
	 * it's a server, not a desktop.
	 *
	 * @see #isServer()
	 * @see #isCalledFromUnitTest()
	 */
	public static boolean isDesktop() {
		return ! OS.equals("Linux");
	}

	/**
	 * Returns true iff this host is running Windows.
	 */
	public static boolean isWindowsOS() {
		return OS.startsWith("Windows");
	}

	/**
	 * Returns true if and only if this method is being called
	 * from code that's running as part of a unit test. This
	 * works by examining the complete call stack, looking for
	 * a method name that starts with "test" in a class whose
	 * name ends with "Test".
	 *
	 * @see #isServer()
	 * @see #isDesktop()
	 * @see #assertIsCalledOnlyFromUnitTest()
	 */
	public static boolean isCalledFromUnitTest() {
		Exception e = new Exception();
		for (StackTraceElement element : e.getStackTrace()) {
			if (isUnitTestClassAndTestMethod(element)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method to be called from a test-only production code method that throws if
	 * that method itself was not called directly from a test class.
	 *
	 * @see #assertIsCalledOnlyFromUnitTest()
	 */
	public static void assertIsCalledDirectlyFromUnitTestClass() {
		Require.isTrue(isUnitTestClass(new Exception().getStackTrace()[2]),
			"Production method not called directly from unit test class.");
	}

	private static boolean isUnitTestClassAndTestMethod(StackTraceElement element) {
		return isUnitTestClass(element) && isTestMethod(element.getMethodName());
	}

	private static boolean isUnitTestClass(StackTraceElement element) {
		return element.getClassName().endsWith("Test");
	}

	/**
	 * If called from a unit test, this method is a no-op. Otherwise, it
	 * throws {@link IllegalStateException}.
	 *
	 * @see #isCalledFromUnitTest()
	 * @see #assertIsCalledDirectlyFromUnitTestClass()
	 */
	public static void assertIsCalledOnlyFromUnitTest() {
		Require.isTrue(Utilities.isCalledFromUnitTest(), "Method should be called only from unit tests!");
	}

	private static boolean isTestMethod(String methodName) {
		return methodName.startsWith("test") ||
			methodName.equals("setUp") ||
			methodName.equals("tearDown");
	}

	/**
	 * Returns true if and only if this method is called on
	 * a non-desktop box and is not called from a unit test.
	 *
	 * @see #isDesktop()
	 * @see #isCalledFromUnitTest()
	 */
	public static boolean isServer() {
		return !isDesktop() && !isCalledFromUnitTest();
	}

	private static final Pattern URL_ENCODED = Pattern.compile("\\+|%[0-9a-fA-F]{2}");

	/**
	 * Returns true if and only if <code>text</code> is URL-encoded, that is,
	 * if it contains one or more '+' (plus) characters, and/or if it contains
	 * one or more instances of a '%' (percent) character followed immediately
	 * by two hexadecimal characters (0-9, a-f, A-F).
	 */
	public static boolean isUrlEncoded(String text) {
		ArgCheck.isNotNull(text, "text");
		return URL_ENCODED.matcher(text).find();
	}

	/**
	 * Returns the decoding of the string <code>s</code> by converting all %XX character
	 * sequences to their ASCII equivalents, and then performing a UTF-8 decoding on the
	 * result. */
	public static String decodeString(String s) {
		return decodeURL(s, /*convertPluses=*/ false);
	}

	/**
	 * Decodes the specified URL string. In particular, replaces strings like "%2F" by
	 * "/", and %3A by ":", and replaces all occurrences of "+" by " " (space). This
	 * method is semantically equivalent to the {@link java.net.URLDecoder#decode(String, String)}
	 * method (using the "UTF-8" character set), but it also does the "+" to " "
	 * conversion and it is implemented more efficiently and handles encoding errors
	 * more gracefully in some cases.
	 *
	 * @throws IllegalArgumentException if <code>url</code> is <code>null</code>
	 * or contains a "%" character not followed immediately by 2 hexadecimal digits.
	 */
	public static String decodeURL(String url) {
		return decodeURL(url, /*convertPluses=*/ true);
	}

	@SuppressWarnings("fallthrough")
	private static String decodeURL(String url, boolean convertPluses) {
		ArgCheck.isNotNull(url, "url");
		StringBuilder sb = null;
		int len = url.length();
		byte[] bytes = null;
		for (int i = 0; i < len; ) {
			char c = url.charAt(i);
			switch (c) {
				case '%':
					sb = initStringBuilder(sb, url, i);
					try {
						// (numChars-i)/3 is an upper bound for the number of remaining bytes
						if (bytes == null) {
							bytes = new byte[(len-i)/3];
						}
						int pos = 0;
						while ((i + 2) < len && c == '%') {
							bytes[pos++] = (byte) Integer.parseInt(url.substring(i+1, i+3), 16);
							i += 3;
							if (i < len) c = url.charAt(i);
						}

						// A trailing, incomplete byte encoding such as
						// "%x" will cause an exception to be thrown
						if (i < len && c == '%') {
							throw new IllegalArgumentException(
								"Incomplete trailing escape (%) pattern");
						}
						sb.append(decodeUTF8(bytes, pos));
					} catch (NumberFormatException ex) {
						String errMsg = "Illegal hex characters in escape (%) pattern - " + ex.getMessage();
						throw new IllegalArgumentException(errMsg, ex);
					}
					break;
				case '+':
					if (convertPluses) {
						sb = initStringBuilder(sb, url, i++);
						sb.append(' ');
						break;
					}
					// fall through if pluses not being converted...
				default:
					if (sb != null) {
						sb.append(c);
					}
					i++;
					break;
			}
		}
		return (sb == null) ? url : sb.toString();
	}

	private static StringBuilder initStringBuilder(StringBuilder sb, String url, int i) {
		if (sb != null) return sb;
		sb = new StringBuilder(url.length());
		sb.append(url.substring(0, i));
		return sb;
	}

	/** The Unicode replacement character inserted in place of decoding errors. */
	private static final char REPLACEMENT_CHAR = '\uFFFD';

	/**
	 * Returns a String for the UTF-8 encoded byte sequence
	 * in <code>bytes[0..len-1]</code>. The length of the resulting
	 * String will be the exact number of characters encoded by these
	 * bytes. Since UTF-8 is a variable-length encoding, the resulting
	 * String may have a length anywhere from len/3 to len, depending
	 * on the contents of the input array.<p>
	 *
	 * In the event of a bad encoding, the UTF-8 replacement character
	 * (code point U+FFFD) is inserted for the bad byte(s), and decoding
	 * resumes from the next byte.
	 */
	/*test*/ static String decodeUTF8(byte[] bytes, int len) {
		char[] res = new char[len];
		int cIx = 0;
		for (int bIx = 0; bIx < len; cIx++) {
			byte b1 = bytes[bIx];
			if ((b1 & 0x80) == 0) {
				// 1-byte sequence (U+0000 - U+007F)
				res[cIx] = (char) b1;
				bIx++;
			} else if ((b1 & 0xE0) == 0xC0) {
				// 2-byte sequence (U+0080 - U+07FF)
				byte b2 = (bIx + 1 < len) ? bytes[bIx+1] : 0; // early end of array
				if ((b2 & 0xC0) == 0x80) {
					res[cIx] = (char) (((b1 & 0x1F) << 6) | (b2 & 0x3F));
					bIx += 2;
				} else {
					// illegal 2nd byte
					res[cIx] = REPLACEMENT_CHAR;
					bIx++; // skip 1st byte
				}
			} else if ((b1 & 0xF0) == 0xE0) {
				// 3-byte sequence (U+0800 - U+FFFF)
				byte b2 = (bIx + 1 < len) ? bytes[bIx+1] : 0; // early end of array
				if ((b2 & 0xC0) == 0x80) {
					byte b3 = (bIx + 2 < len) ? bytes[bIx+2] : 0; // early end of array
					if ((b3 & 0xC0) == 0x80) {
						res[cIx] = (char) (((b1 & 0x0F) << 12) | ((b2 & 0x3F) << 6) | (b3 & 0x3F));
						bIx +=3;
					} else {
						// illegal 3rd byte
						res[cIx] = REPLACEMENT_CHAR;
						bIx += 2; // skip 1st TWO bytes
					}
				} else {
					// illegal 2nd byte
					res[cIx] = REPLACEMENT_CHAR;
					bIx++; // skip 1st byte
				}
			} else {
				// illegal 1st byte
				res[cIx] = REPLACEMENT_CHAR;
				bIx++; // skip 1st byte
			}
		}
		return new String(res, 0, cIx);
	}

	/**
	 * Convert a CamelCase identifier to a standard database identifier, by
	 * inserting an underscore between "words" and forcing all upper-case letters
	 * to lower-case. For example, "QueryString" becomes "query_string".
	 *
	 * @param id
	 *            The id to convert
	 * @return The database version of the input parameter.
	 */
	public static String asDatabaseID(String id) {
		return convertCamelCaseToCharSeparatedString(id, '_');
	}

	/**
	 * Convert a CamelCase string to a string with the specified separator character.  Inserts the specified
	 * character between "words" and forces all upper-case letters to lower-case.
	 *
	 * @param str The string to change
	 * @param separator the separator character to use
	 * @return The word separated version of the input string
	 */
	public static String convertCamelCaseToCharSeparatedString(String str, char separator) {
		int N = str.length();
		StringBuilder sb = new StringBuilder(2 * N);
		for (int i = 0; i < N; i++) {
			char ch = str.charAt(i);
			if (Character.isUpperCase(ch)) {
				if (i > 0) {
					sb.append(separator);
				}
				ch = Character.toLowerCase(ch);
			}
			sb.append(ch);
		}
		return sb.toString();
	}

	/**
	 * Convert an identifier into CamelCase, by capitalizing the first letter
	 * and the letter following all underscores, and then removing all
	 * underscores. For example, "query_string" becomes "QueryString".
	 *
	 * @param id
	 *            The id to convert
	 * @return The CamelCase version of the input parameter.
	 */
	public static String toCamelCase(String id) {
		int N = id.length();
		StringBuilder sb = new StringBuilder(2 * N);
		boolean makeUpper = true;
		for (int i = 0; i < N; i++) {
			char ch = id.charAt(i);
			if (ch == '_') {
				makeUpper = true;
			} else {
				if (makeUpper) {
					ch = Character.toUpperCase(ch);
					makeUpper = false;
				}
				sb.append(ch);
			}
		}
		return sb.toString();
	}

	/**
	 * Returns the number of dot-separated components in <code>s</code>. This
	 * is just the number of occurences of "." in <code>s</code> plus 1. Examples:
	 * <pre>
	 * abc -> 1
	 * ab.c -> 2
	 * a.b.c -> 3
	 * .b. -> 3
	 * </pre>
	 */
	public static int numComponents(String s) {
		if (s == null) {
			return 0;
		}
		int curr = 0, numDots = 0, ix;
		while ((ix = s.indexOf('.', curr)) >= 0) {
			numDots ++;
			curr = ix + 1;
		}
		return numDots + 1;
	}

	/**
	 * Returns the first component of <code>s</code> in dotted-notation, i.e.,
	 * everything before the leftmost dot. For example,
	 * "firstComponent(mithra.anchorintelligence.com)" would return "mithra".
	 *
	 * @param s
	 *            The string to consider.
	 * @return The part of <code>s</code> that precedes the leftmost dot, or
	 *         all of <code>s</code> if it contains no dots.
	 */
	public static String firstComponent(String s) {
		int dotIx = s.indexOf('.');
		return dotIx < 0 ? s : s.substring(0, dotIx);
	}

	/**
	 * Returns the last component of <code>s</code> in dotted-notation, i.e.,
	 * everything after the rightmost dot. For example,
	 * <em>lastComponent("v.p1r.log.uid")</em> would return "uid".
	 *
	 * @param s
	 *            The string to consider.
	 * @return The part of <code>s</code> that follows the rightmost dot, or
	 *         all of <code>s</code> if it contains no dots.
	 */
	public static String lastComponent(String s) {
		return lastComponents(s, 1);
	}

	/**
	 * Returns the last <code>n</code> dot-separated components of
	 * <code>s</code>. If <code>s</code> has fewer than <code>n</code>
	 * components, then all of <code>s</code> is returned. Examples:
	 * <pre>
	 * lastComponents("a.b.c", 1) -> "c"
	 * lastComponents("a.b.c", 2) -> "b.c"
	 * lastComponents("a.b.c", 3) -> "a.b.c"
	 * lastComponents("a.b.c", 4) -> "a.b.c"
	 * lastComponents("foo", 1)   -> "foo"
	 * </pre>
	 *
	 * @param s
	 *            The string to consider.
	 * @param n
	 *            The number of final components of <code>s</code> to return.
	 *            This value must be > 0.
	 */
	public static String lastComponents(String s, int n) {
		ArgCheck.isTrue(n > 0, "Number of components 'n' must be strictly positive.");
		int dotIx = s.lastIndexOf('.');
		while (dotIx >= 0 && --n > 0) {
			dotIx = s.lastIndexOf('.', dotIx - 1);
		}
		return s.substring(dotIx + 1);
	}

	/**
	 * Returns the string <code>s</code> with the last component removed. For example,
	 * the input "v.p1r.log.uid" would produce the result "v.p1r.log". If <code>s</code>
	 * contains no "." characters, the arguemnt <code>s</code> is returned.
	 *
	 * @param s
	 *            The string to consider
	 * @return The part of s that precedes the rightmost dot.
	 */
	public static String chopLastComponent(String s) {
		int lastDotIx = s.lastIndexOf('.');
		return lastDotIx < 0 ? s : s.substring(0, lastDotIx);
	}

	/**
	 * Returns the name of a file with the trailing .gz removed (if present).
	 */
	public static String getUncompressedFilename(String baseName) {
		if (baseName.endsWith(".gz")) {
			return chopLastComponent(baseName);
		}
		return baseName;
	}

	/**
	 * Returns the string <code>s</code> with the first component removed. For example,
	 * the input "v.p1r.log.uid" would produce the result "p1r.log.uid". If <code>s</code>
	 * contains no "." characters, the argument <code>s</code> is returned.
	 *
	 * @param s
	 *            The string to consider
	 * @return The part of s following the leftmost dot.
	 */
	public static String chopFirstComponent(String s) {
		int dotIx = s.indexOf('.');
		return dotIx < 0 ? s : s.substring(1 + dotIx);
	}

	/**
	 * Returns the specified string as a char value, raising an exception if the
	 * string is not a single character. (Is there a Java String or Character
	 * function to do this?)
	 */
	public static char parseChar(String s) {
		ArgCheck.isNotBlank(s, "s");
		if (s.length() > 1) {
			throw new java.lang.IllegalArgumentException(
				"parseChar: argument string contains multiple characters");
		}
		return s.charAt(0);
	}

	/**
	 * Returns a version of the string <code>s</code> that is at most
	 * <code>N</code> characters long, replacing the final characters of
	 * <code>s</code> with ellipses when <code>s</code> is longer than
	 * <code>N</code>.
	 *
	 * However, ellipses are not added if <code>N<=3</code> (since the
	 * returned string would just contain periods).
	 */
	public static String truncateStringIfNecessary(String s, int N) {
		ArgCheck.isTrue(N >= 0, "N must be non-negative");
		final String ELLIPSES = "...";
		final int ELLIPSES_LENGTH = ELLIPSES.length();

		if (s.length() > N) {
			if (N > ELLIPSES_LENGTH) {
				return s.substring(0, N - ELLIPSES_LENGTH) + ELLIPSES;
			}
			return s.substring(0, N);
		}
		return s;
	}

	/**
	 * Returns boolean equivalent of the string <code>s</code>. Most of the time,
	 * booleans will be serialized as a "0" or a "1", so special-case those
	 * values, and then revert to the standard {@link Boolean#parseBoolean}
	 * method. Note that if <code>s</code> is neither "0", "1", or "true" (case
	 * insensitive), the result will be false. This includes the case where
	 * <code>s</code> is <code>null</code>.
	 */
	public static boolean parseBoolean(String s) {
		if ("1".equals(s) || "y".equalsIgnoreCase(s)) {
			return true;
		} else if ("0".equals(s) || "n".equalsIgnoreCase(s)) {
			return false;
		} else {
			return Boolean.parseBoolean(s);
		}
	}

	/**
	 * Returns "0" or "1" as <code>b</code> if false or true, respectively.
	 */
	public static String booleanToOneZero(boolean b) {
		return (b) ? "1" : "0";
	}

	/**
	 * Returns the String <code>s</code> as a String. If <code>s</code> is
	 * the string-literal "NULL" or "\N", returns <code>null</code>; returns
	 * <code>s</code> otherwise. Provided for compatibility with MySQL
	 * import/export.
	 */
	public static String parseDbString(String s) {
		if ("NULL".equals(s) || "\\N".equals(s)) {
			return null;
		}
		return s;
	}

	/**
	 * Converts the String <code>s</code> into something formatted properly
	 * for MySQL. The current implementation simply converts a null input to the
	 * String "NULL"; returns <code>s</code> otherwise.
	 */
	public static String stringToDbString(String s) {
		return (s == null) ? "NULL" : s;
	}

	/**
	 * Converts the BigDecimal {@code bd} into something formatted properly
	 * for MySQL. The current implementation simply converts a null input to the
	 * String "NULL"; returns {@code bd.toString()} otherwise.
	 */
	public static String bigDecimalToDbString(BigDecimal bd) {
		return (bd == null) ? "NULL" : bd.toString();
	}

	/**
	 * Returns the String representation of <code>e</code> if it is non-<code>null</code>;
	 * returns <code>null</code> otherwise.
	 */
	public static String enumToString(Enum<?> e) {
		return e == null ? null : e.toString();
	}

	public static String toStringFromChar(char value) {
		return String.valueOf(value);
	}

	public static char toCharFromString(String value) {
		if (value == null || value.length() != 1) {
			throw new IllegalStateException("Bad value for character: " + value);
		}
		return value.charAt(0);
	}

	/**
	 * Returns the string encoded by the successive pairs of hexidecimal
	 * values in <code>hex</code>. For example, since hex value 0x63
	 * corresponds to ASCII character 'c', 0x61 to character 'a', and
	 * 0x74 to character 't', the input "636174" would result in the
	 * returned string "cat".
	 *
	 * @throws IllegalArgumentException if <code>hex</code> has an odd length
	 * @throws NumberFormatException if any successive pair of characters in
	 * <code>hex</code> cannot be parsed as a base-16 value.
	 */
	public static String hex2string(String hex) {
		ArgCheck.isTrue(hex.length() % 2 == 0, "hex string has odd length");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i + 1 < hex.length(); i += 2) {
			sb.append((char) Integer.parseInt(hex.substring(i, i + 2), 16));
		}
		return sb.toString();
	}

	/** Returns the empty string if <code>s</code> is null; otherwise, returns <code>s</code>. */
	public static String convertNullToEmptyString(String s) {
		return (s == null) ? "" : s;
	}

	/**
	 * Converts a timestamp that has milliseconds set to a timestamp with milliseconds set to zero.
	 */
	public static long stripMillisecondsFromTimestamp(long timestamp) {
		return timestamp == -1 ? timestamp : timestamp - (timestamp % DateUtils.MILLIS_PER_SECOND);
	}

	private static final Pattern NUMBER_STRING_PATTERN = Pattern.compile("(\\d+)(\\p{Alpha}+)");

	public static long parseDuration(String duration) {
		Matcher matcher = NUMBER_STRING_PATTERN.matcher(duration);
		if (matcher.matches()) {
			int value = Integer.parseInt(matcher.group(1));
			String timeUnit = matcher.group(2);

			if (timeUnit.equals("w")) {
				return 7L * DateUtils.MILLIS_PER_DAY * value;
			} else if (timeUnit.equals("d")) {
				return DateUtils.MILLIS_PER_DAY * value;
			} else if (timeUnit.equalsIgnoreCase("h")) {
				return DateUtils.MILLIS_PER_HOUR * value;
			} else if (timeUnit.equals("m")) {
				return DateUtils.MILLIS_PER_MINUTE * value;
			} else if (timeUnit.equals("s")) {
				return DateUtils.MILLIS_PER_SECOND * value;
			} else if (timeUnit.equals("ms")) {
				return value;
			}
		}
		throw new IllegalArgumentException(
			"duration [" + duration + "] must end with a duration element: "
			+ "w(eeks), d(ays), h(ours) or H(ours), m(inutes), s(econds), or ms(milliseconds)");
	}

	/** Returns <code>obj</code> if it is non-null; otherwise, returns <code>defaultVal</code>. */
	public static <T> T ifNull(T obj, T defaultVal) {
		return (obj == null) ? defaultVal : obj;
	}

	/**
	 * Returns true iff the collection <code>c</code> contains any of the elements in <code>elts</code>.
	 */
	public static <T> boolean collectionContainsAny(Collection<T> c, T... elts) {
		for (T elt: elts) {
			if (c.contains(elt)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Assigns the specified T reference <code>val</code> to each element of the
	 * array <code>a</code>.
	 *
	 * @param a the array to be filled.
	 * @param val the value to be stored in all elements of the array.
	 * @return a
	 */
	public static <T> T[] fill(/*INOUT*/ T[] a, T val) {
		Arrays.fill(a, val);
		return a;
	}

	/**
	 * Returns a signed 64-bit value of the hex string (for example E8E8A462D453496F).
	 */
	public static long parseHexLong(String s) {
		ArgCheck.isNotNull(s, "s");
		int pos = s.length() - 8;
		long high = pos <= 0 ? 0 : Long.parseLong(s.substring(0, pos), 16);
		long low = Long.parseLong(s.substring(Math.max(pos,0)), 16);
		return high << 32 | low;
	}
}
