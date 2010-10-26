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


/**
 * Tests the {@link IpAddressRange} implementation.
 *
 * @author Allan Heydon
 */
public class IpAddressRangeTest extends AbstractAnchorTest {

	// ================================================= constructors

	public void testSimpleConstructorSetsAllThreeFields() {
		IpAddressRange range = new IpAddressRange(123L, 456L, 789L);
		assertEquals(123L, range.getStartIp());
		assertEquals(456L, range.getEndIp());
		assertEquals(789L, range.getClusterIp());
	}

	public void testCidrConstructorThrowsIfNoSlashCharacter() {
		checkCidrConstructorThrowsIllegalArgumentException("192.168.0.0-16",
			"CIDR address string missing '/': 192.168.0.0-16");
	}

	public void testCidrConstructorThrowsIfPrefixLengthIsNotANumber() {
		checkCidrConstructorThrowsIllegalArgumentException(
			"192.168.0.0/16a",
			"Prefix length is not a number: 16a");
	}

	public void testCidrConstructorThrowsIfPrefixLengthIsTooLargeOrSmall() {
		checkCidrConstructorThrowsIfPrefixLengthIsTooLargeOrSmall(-1);
		checkCidrConstructorThrowsIfPrefixLengthIsTooLargeOrSmall(33);
	}

	private void checkCidrConstructorThrowsIfPrefixLengthIsTooLargeOrSmall(int prefixLen) {
		checkCidrConstructorThrowsIllegalArgumentException(
			"192.168.0.0/" + prefixLen,
			"Prefix length is not in the range [0,32]: " + prefixLen);
	}

	public void testCidrConstructorThrowsIfCidrHasMaskedLowOrderBitsSet() {
		for (int i = 0; i < 32; i++) {
			long ip = ((1L << 32) - 1L) ^ ((1L << (32 - i - 1)) - 1);
			checkCidrConstructorThrowsIfCidrHasMaskedLowOrderBitsSet(1L, i);
			checkCidrConstructorThrowsIfCidrHasMaskedLowOrderBitsSet(ip, i);
			checkCidrConstructorThrowsIfCidrHasMaskedLowOrderBitsSet(ip | 1L, i);
		}
	}

	private void checkCidrConstructorThrowsIfCidrHasMaskedLowOrderBitsSet(
			long ip, int prefixLen) {
		String cidr = IpAddressUtils.formatIp(ip) + "/" + prefixLen;
		checkCidrConstructorThrowsIllegalArgumentException(cidr,
			"CIDR expression has non-zero low-order bits: " + cidr);
	}

	private void checkCidrConstructorThrowsIllegalArgumentException(
			String cidrStr, String expErrorMessage)
	{
		try {
			new IpAddressRange(cidrStr, 123L);
			fail("Bad CIDR string: " + cidrStr);
		} catch (IllegalArgumentException ex) {
			assertEquals(expErrorMessage, ex.getMessage());
		}
	}

	public void testCidrConstructorSetsFieldsCorrectly() {
		checkCidrConstructorSetsFieldsCorrectly(
				"123.234.240.0/20", "123.234.240.0", "123.234.255.255");
		checkCidrConstructorSetsFieldsCorrectly(
				"123.234.240.0/21", "123.234.240.0", "123.234.247.255");
		checkCidrConstructorSetsFieldsCorrectly(
				"123.234.240.0/23", "123.234.240.0", "123.234.241.255");
		checkCidrConstructorSetsFieldsCorrectly(
				"123.234.240.0/24", "123.234.240.0", "123.234.240.255");

		// private IP address ranges
		checkCidrConstructorSetsFieldsCorrectly(
				"10.0.0.0/8", "10.0.0.0", "10.255.255.255");
		checkCidrConstructorSetsFieldsCorrectly(
				"172.16.0.0/12", "172.16.0.0", "172.31.255.255");
		checkCidrConstructorSetsFieldsCorrectly(
				"192.168.0.0/16", "192.168.0.0", "192.168.255.255");
	}

	private void checkCidrConstructorSetsFieldsCorrectly(
			String cidrStr, String expStartIp, String expEndIp)
	{
		IpAddressRange range = new IpAddressRange(cidrStr, 0x12345678L);
		assertEquals(ipToLong(expStartIp), range.getStartIp());
		assertEquals(ipToLong(expEndIp), range.getEndIp());
		assertEquals(0x12345678L, range.getClusterIp());
	}

	// ================================================= containsIp

	public void testContainsIpReturnsCorrectResults() {
		IpAddressRange range = new IpAddressRange("172.16.0.0/12", 1L);

		// in-range values
		assertTrue(range.containsIp(ipToLong("172.16.0.0")));
		assertTrue(range.containsIp(ipToLong("172.16.0.1")));
		assertTrue(range.containsIp(ipToLong("172.16.255.0")));
		assertTrue(range.containsIp(ipToLong("172.16.255.255")));
		assertTrue(range.containsIp(ipToLong("172.31.0.0")));
		assertTrue(range.containsIp(ipToLong("172.31.255.254")));
		assertTrue(range.containsIp(ipToLong("172.31.255.255")));

		// out-of-range values
		assertFalse(range.containsIp(ipToLong("0.0.0.0")));
		assertFalse(range.containsIp(ipToLong("0.255.255.255")));
		assertFalse(range.containsIp(ipToLong("172.0.0.0")));
		assertFalse(range.containsIp(ipToLong("172.15.0.0")));
		assertFalse(range.containsIp(ipToLong("172.15.255.255")));
		assertFalse(range.containsIp(ipToLong("172.32.0.0")));
		assertFalse(range.containsIp(ipToLong("172.32.255.255")));
		assertFalse(range.containsIp(ipToLong("172.255.255.255")));
		assertFalse(range.containsIp(ipToLong("255.0.0.0")));
		assertFalse(range.containsIp(ipToLong("255.255.255.255")));
	}

	// ================================================= private helpers

	private long ipToLong(String ip) {
		return IpAddressUtils.ipToLong(ip);
	}
}
