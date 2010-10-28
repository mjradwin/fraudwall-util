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
import com.fraudwall.util.exc.Require;


/**
 * A hodge-podge of general-purpose utility functions.
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
				host = com.fraudwall.util.StringUtils.firstComponent(InetAddress.getLocalHost().getHostName());
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

	/**
	 * Returns the name of a file with the trailing .gz removed (if present).
	 */
	public static String getUncompressedFilename(String baseName) {
		if (baseName.endsWith(".gz")) {
			return com.fraudwall.util.StringUtils.chopLastComponent(baseName);
		}
		return baseName;
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
}
