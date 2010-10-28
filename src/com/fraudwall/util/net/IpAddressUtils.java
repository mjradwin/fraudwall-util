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
package com.fraudwall.util.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.fraudwall.util.exc.AnchorFatalError;
import com.fraudwall.util.exc.ArgCheck;

/**
 * Defines utility methods for working with IP Addresses.
 * Anchor Intelligence currently represents each IP address
 * with a Java <code>long</code> value. When specified as
 * strings, IP addresses take the form "W.X.Y.Z", where
 * the four octets W, X, Y, and Z, are each base-10 integers
 * in the closed interval [0,255].
 *
 * @author Allan Heydon
 */
public abstract class IpAddressUtils {
	/**
	 * Returns the value of the top 8 bits (the first octet)
	 * of the IP address <code>ip</code>. For example, the
	 * expression getIp8(ipToLong("56.32.45.78")) would return
	 * 56.
	 */
	public static int getIp8(long ip) {
		return (int)((ip >> 24) & 0xff);
	}
	/**
	 * Returns the value of the top 16 bits (the first two octets)
	 * of the IP address <code>ip</code>. For example, the
	 * expression getIp16(ipToLong("56.32.45.78")) would return
	 * (56 * 256) + 32 = 14368.
	 */
	public static int getIp16(long ip) {
		return (int)((ip >> 16) & 0xffff);
	}
	/**
	 * Returns the value of the top 24 bits (the first three octets)
	 * of the IP address <code>ip</code>. For example, the
	 * expression getIp24(ipToLong("56.32.45.78")) would return
	 * (56 * (256^2)) + (32 * 256) + 45 = 3678253.
	 */
	public static int getIp24(long ip) {
		return (int)((ip >> 8) & 0xffffff);
	}

	/**
	 * Returns an IP address string of the form "W.X.Y.Z" for the
	 * IP address value <code>ip</code>. Each of the four octets
	 * W, X, Y, and Z will be a base-10, non-padded value in the interval
	 * [0, 255].
	 *
	 * @see #ipToLong(String)
	 */
	public static String formatIp(long ip) {
		String res = "";
		for (int i = 3; i >= 0; i--) {
			res += 0xff & (ip >> (8 * i));
			if (i > 0) res += ".";
		}
		return res;
	}

	/**
	 * Returns an array containing the results of invoking {@link #formatIp(long)}
	 * on each of the IP values <code>ipNums</code>. The results are in the
	 * same order as the argument values.
	 */
	public static String[] formatIps(long... ipNums) {
		String[] res = new String[ipNums.length];
		for (int i = 0; i < ipNums.length; i++) {
			res[i] = formatIp(ipNums[i]);
		}
		return res;

	}

	/**
	 * Returns a positive long number for the given IP address. This implements
	 * a one-to-one mapping.
	 *
	 * @param ipAddr
	 *            The IP address, in the form "X.Y.Z.W", where each of the four
	 *            octets is a value in the interval [0, 255].
	 *
	 * @throws IllegalArgumentException
	 *             If <code>ip</code> does not consist of four numeric octets,
	 *             each of whose values is in the interval [0, 255].
	 *
	 * @see #formatIp(long)
	 */
	public static long ipToLong(String ipAddr) {
		String[] octets = ipAddr.split("\\.");
		if (octets.length != 4) { // guard against unnecessary ArgCheck calls
			ArgCheck.equals(4, octets.length, "Wrong number of octets in ip [" + ipAddr + "]");
		}
		long ipb = 0;
		for (int i = 0; i < 4; i++) {
			ipb = (ipb << 8) + IpAddressUtils.parseOctetString(ipAddr, octets[i]);
		}
		return ipb;
	}

	/**
	 * Returns the numeric value of the given {@code ipAddr}, which may either be
	 * an IP address expressed in dotted-quad notation (e.g., "67.49.75.119"), or
	 * a Long numeric value (e.g., "1127304055").
	 */
	public static long smartParse(String ipAddr) {
		return ipAddr.indexOf('.') > 0 ? ipToLong(ipAddr) : Long.parseLong(ipAddr);
	}

	/**
	 * Returns an array containing the results of invoking {@link #ipToLong(String)}
	 * on each of the IP addresses in <code>ipAddrs</code>. The results are in the
	 * same order as the argument values.
	 */
	public static long[] ipsToLongs(String... ipAddrs) {
		long[] res = new long[ipAddrs.length];
		for (int i = 0; i < ipAddrs.length; i++) {
			res[i] = ipToLong(ipAddrs[i]);
		}
		return res;
	}

	private static long parseOctetString(String ip, String octet) {
		long octetVal;
		try {
			octetVal = Long.parseLong(octet);
		} catch (NumberFormatException ex) {
			octetVal = -1; // signal an error
		}
		if (octetVal < 0 || octetVal > 255) { // guard against unnecessary ArgCheck calls
			ArgCheck.isTrue(false, // always fail
				"IP address [" + ip + "] contains illegal octet [" + octet + "]");
		}
		return octetVal;
	}

	static final IpAddressRange[] PRIVATE_IP_RANGES = new IpAddressRange[] {
		new IpAddressRange("10.0.0.0/8", 0L), // 10.0.0.0 - 10.255.255.255
		new IpAddressRange("127.0.0.0/8", 0L), // 127.0.0.0 - 127.255.255.255
		new IpAddressRange("172.16.0.0/12", 0L), // 172.16.0.0 - 172.31.255.255
		new IpAddressRange("192.168.0.0/16", 0L) //192.168.0.0 - 192.168.255.255
	};

	/**
	 * Returns true if and only if the IP address represented by
	 * <code>ip</code> is in one of the three IP ranges defined to be private
	 * by <a href="http://www.faqs.org/rfcs/rfc1918.html">RFC 1918</a>,
	 * Section 3, or is in the loopback range defined by
	 * <a href="http://www.faqs.org/rfcs/rfc3330.html">RFC 3330</a>. To get
	 * an IP address represented as a long, use {@link #ipToLong}.
	 */
	public static boolean isPrivateIPAddress(long ip) {
		for (int i=0; i < IpAddressUtils.PRIVATE_IP_RANGES.length; i++) {
			if (IpAddressUtils.PRIVATE_IP_RANGES[i].containsIp(ip)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the Java {@link InetAddress} for the given IP address, in dotted-quad
	 * notation.
	 *
	 * @see #ipToInetAddress(long)
	 */
	public static InetAddress ipToInetAddress(String ipAddr) {
		return ipToInetAddress(ipToLong(ipAddr));
	}

	/**
	 * Returns the Java {@link InetAddress} for the given IP number. The argument
	 * is the long representation of an IP address.
	 *
	 * @see #ipToLong(String)
	 * @see #ipToInetAddress(String)
	 */
	public static InetAddress ipToInetAddress(long ipnum) throws AnchorFatalError {
		byte[] bytes = new byte[4];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) ((ipnum >> ((3 - i) * 8)) & 0xff);
		}
		try {
			return InetAddress.getByAddress(bytes);
		} catch (UnknownHostException ex) {
			throw new AnchorFatalError("programming error in IpAddressUtils.ipToInetAddress");
		}
	}
}
