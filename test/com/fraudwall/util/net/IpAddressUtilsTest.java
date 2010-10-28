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

import com.fraudwall.util.AbstractAnchorTest;
import com.fraudwall.util.net.IpAddressUtils;


/**
 * Tests the {@link IpAddressUtilsTest} implementation.
 *
 * @author Allan Heydon
 */
public class IpAddressUtilsTest extends AbstractAnchorTest {

	/** {@link IpAddressUtils#getIp8(long)} ------------------------------------------------------------------- */

	public void testGetIp8ReturnsCorrectValue() {
		checkGetIp8ReturnsCorrectValue("56.32.45.78", 56);
		checkGetIp8ReturnsCorrectValue("56.0.0.0", 56);
		checkGetIp8ReturnsCorrectValue("56.255.255.255", 56);
		checkGetIp8ReturnsCorrectValue("254.0.0.0", 254);
		checkGetIp8ReturnsCorrectValue("254.255.255.255", 254);
	}

	private void checkGetIp8ReturnsCorrectValue(String ip, int exp) {
		long ipVal = IpAddressUtils.ipToLong(ip);
		assertEquals(exp, IpAddressUtils.getIp8(ipVal));
	}

	/** {@link IpAddressUtils#getIp16(long)} ------------------------------------------------------------------ */

	public void testGetIp16ReturnsCorrectValue() {
		checkGetIp16ReturnsCorrectValue("56.32.45.78", 56, 32);
		checkGetIp16ReturnsCorrectValue("56.32.0.0", 56, 32);
		checkGetIp16ReturnsCorrectValue("56.32.255.255", 56, 32);
		checkGetIp16ReturnsCorrectValue("254.253.0.0", 254, 253);
		checkGetIp16ReturnsCorrectValue("254.253.255.255", 254, 253);
	}

	private void checkGetIp16ReturnsCorrectValue(String ip, int oct1, int oct2) {
		long ipVal = IpAddressUtils.ipToLong(ip);
		int exp = (oct1 * 256) + oct2;
		assertEquals(exp, IpAddressUtils.getIp16(ipVal));
	}

	/** {@link IpAddressUtils#getIp24(long)} ------------------------------------------------------------------ */

	public void testGetIp24ReturnsCorrectValue() {
		checkGetIp24ReturnsCorrectValue("56.32.45.78", 56, 32, 45);
		checkGetIp24ReturnsCorrectValue("56.32.45.0", 56, 32, 45);
		checkGetIp24ReturnsCorrectValue("56.32.45.255", 56, 32, 45);
		checkGetIp24ReturnsCorrectValue("254.253.252.0", 254, 253, 252);
		checkGetIp24ReturnsCorrectValue("254.253.252.255", 254, 253, 252);
	}

	private void checkGetIp24ReturnsCorrectValue(String ip, int oct1, int oct2, int oct3) {
		long ipVal = IpAddressUtils.ipToLong(ip);
		int exp = (oct1 * 256 * 256) + (oct2 * 256) + oct3;
		assertEquals(exp, IpAddressUtils.getIp24(ipVal));
	}

	/** {@link IpAddressUtils#formatIp(long)} ----------------------------------------------------------------- */

	public void testFormatIpReturnsCorrectValue() {
		checkFormatIpReturnsCorrectValue("0.0.0.0");
		checkFormatIpReturnsCorrectValue("1.2.3.4");
		checkFormatIpReturnsCorrectValue("192.168.1.2");
		checkFormatIpReturnsCorrectValue("10.100.234.123");
		checkFormatIpReturnsCorrectValue("255.255.255.255");
	}

	private void checkFormatIpReturnsCorrectValue(String ipStr) {
		long ip = IpAddressUtils.ipToLong(ipStr);
		assertEquals(ipStr, IpAddressUtils.formatIp(ip));
	}

	/** {@link IpAddressUtils#formatIps(long...)} ------------------------------------------------------------- */

	public void testFormatIpsReturnsEmptyResultForEmptyArgument() {
		assertEquals(0, IpAddressUtils.formatIps().length);
	}

	public void testFormatIpsReturnsCorrectResult() {
		String[] ipAddrs = new String[] { "0.0.0.0", "1.2.3.4", "192.168.1.2", "255.255.255.255" };
		String[] res = IpAddressUtils.formatIps(IpAddressUtils.ipsToLongs(ipAddrs));
		assertArrayEquals(ipAddrs, res);
	}

	/** {@link IpAddressUtils#ipToLong(String)} --------------------------------------------------------------- */

	public void testIpToLongThrowsIfNotExactly4Octets() {
		checkIpToLongThrowsIfNotExactly4Octets("");
		checkIpToLongThrowsIfNotExactly4Octets("1");
		checkIpToLongThrowsIfNotExactly4Octets("1000");
		checkIpToLongThrowsIfNotExactly4Octets("1.2");
		checkIpToLongThrowsIfNotExactly4Octets("1.2.3");
		checkIpToLongThrowsIfNotExactly4Octets("1.2.3.4.5");
	}

	private void checkIpToLongThrowsIfNotExactly4Octets(String ip) {
		try {
			IpAddressUtils.ipToLong(ip);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			assertTrue(ex.getMessage().startsWith("Wrong number of octets in ip ["+ip+"]"));
		}
	}

	public void testIpToLongThrowsIfOctetOutOfRange() {
		checkIpToLongThrowsIfOctetIllegal("1.2.-1.4", "-1");
		checkIpToLongThrowsIfOctetIllegal("1.2.256.257", "256");
	}

	public void testIpToLongThrowsIfOctetIsNonNumber() {
		checkIpToLongThrowsIfOctetIllegal("1.2.abc.4", "abc");
		checkIpToLongThrowsIfOctetIllegal("1.2.12a.abc", "12a");
	}

	private void checkIpToLongThrowsIfOctetIllegal(String ip, String badOctet) {
		try {
			IpAddressUtils.ipToLong(ip);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			assertEquals("IP address [" + ip + "] contains illegal octet [" + badOctet + "]", ex.getMessage());
		}
	}

	public void testIpToLongConvertsToUnsignedLongCorrectly() {
		checkIpToLongConvertsToUnsignedLongCorrectly("0.0.0.0", 0x0L);
		checkIpToLongConvertsToUnsignedLongCorrectly("0.0.0.1", 0x1L);
		checkIpToLongConvertsToUnsignedLongCorrectly("1.0.0.0", 0x01000000L);
		checkIpToLongConvertsToUnsignedLongCorrectly("0.1.2.3", 0x00010203L);
		checkIpToLongConvertsToUnsignedLongCorrectly("128.128.128.128", 0x80808080L);
		checkIpToLongConvertsToUnsignedLongCorrectly("255.255.255.255", 0xffffffffL);
	}

	private void checkIpToLongConvertsToUnsignedLongCorrectly(String ip, long exp) {
		assertEquals(exp, IpAddressUtils.ipToLong(ip));
	}

	/** {@link IpAddressUtils#smartParse(String)} ------------------------------------------------------------- */

	public void testSmartParseCorrectlyHandlesDottedQuadAddress() {
		assertEquals(1127304055L, IpAddressUtils.smartParse("67.49.75.119"));
	}

	public void testSmartParseCorrectlyHandlesNumericAddresses() {
		assertEquals(1127304055L, IpAddressUtils.smartParse("1127304055"));
	}

	/** {@link IpAddressUtils#ipsToLongs(String...)} ---------------------------------------------------------- */

	public void testIpsToLongsReturnsEmptyResultForEmptyArgument() {
		assertEquals(0, IpAddressUtils.ipsToLongs().length);
	}

	public void testIpsToLongsReturnsCorrectResult() {
		String[] ipAddrs = new String[] { "0.0.0.0", "1.2.3.4", "192.168.1.2", "255.255.255.255" };
		long[] ips = IpAddressUtils.ipsToLongs(ipAddrs);
		assertEquals(ipAddrs.length, ips.length);
		for (int i = 0; i < ips.length; i++) {
			assertEquals(IpAddressUtils.ipToLong(ipAddrs[i]), ips[i]);
		}
	}

	/** {@link IpAddressUtils#isPrivateIPAddress(long)} ------------------------------------------------------- */

	public void testIsPrivateIPAddressReturnsTrueForPrivateIPs() {
		assertTrue(isPrivateIPAddress("10.0.0.0"));
		assertTrue(isPrivateIPAddress("10.255.255.255"));
		assertTrue(isPrivateIPAddress("127.0.0.0"));
		assertTrue(isPrivateIPAddress("127.255.255.255"));
		assertTrue(isPrivateIPAddress("172.16.0.0"));
		assertTrue(isPrivateIPAddress("172.31.255.255"));
		assertTrue(isPrivateIPAddress("192.168.0.0"));
		assertTrue(isPrivateIPAddress("192.168.255.255"));
	}

	public void testIsPrivateIPAddressReturnsFalseForNonPrivateBoundaryIPs() {
		assertFalse(isPrivateIPAddress("172.0.0.0"));
		assertFalse(isPrivateIPAddress("172.15.255.255"));
		assertFalse(isPrivateIPAddress("172.32.0.0"));
		assertFalse(isPrivateIPAddress("172.255.255.255"));
		assertFalse(isPrivateIPAddress("192.0.0.0"));
		assertFalse(isPrivateIPAddress("192.167.255.255"));
		assertFalse(isPrivateIPAddress("192.169.0.0"));
		assertFalse(isPrivateIPAddress("192.255.255.255"));
	}

	public void testIsPrivateIPAddressReturnsFalseForNonPrivateBulkIPs() {
		for (int i = 0; i < 256; i++) {
			if (i != 10 && i != 127 && i != 172 && i != 192) {
				assertFalse(isPrivateIPAddress(i + ".0.0.0"));
				assertFalse(isPrivateIPAddress(i + ".255.255.255"));
			}
		}
	}

	private boolean isPrivateIPAddress(String ip) {
		long ipNum = IpAddressUtils.ipToLong(ip);
		return IpAddressUtils.isPrivateIPAddress(ipNum);
	}

	/** {@link IpAddressUtils#ipToInetAddress(String)} -------------------------------------------------------- */

	public void testIpToInetAddressOnStringThrowsIfArgumentNotInDottedQuadNotation() {
		try {
			IpAddressUtils.ipToInetAddress("16.10.250");
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			assertEquals("Wrong number of octets in ip [16.10.250]; expected 4, got 3", ex.getMessage());
		}
	}

	public void testIpToInetAddressOnStringReturnsCorrectResult() {
		InetAddress inet = IpAddressUtils.ipToInetAddress("16.10.250.130");
		assertEquals("/16.10.250.130", inet.toString());
	}

	/** {@link IpAddressUtils#ipToInetAddress(long)} ---------------------------------------------------------- */

	public void testIpToInetAddressOnLongReturnsCorrectResult() {
		InetAddress inet = IpAddressUtils.ipToInetAddress(1234567890L);
		assertEquals("/" + IpAddressUtils.formatIp(1234567890L), inet.toString());
	}
}
