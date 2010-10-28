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


/**
 * A range of IP addresses paired with a canonical "cluster" IP address
 * to which any IP address in the range should be mapped. The IP address
 * range is specified by a CIDR specifier of the form "W.X.Y.Z/B".
 */
public class IpAddressRange {
	private final long startIp;
	private final long endIp;
	private final long clusterIp;

	/**
	 * Constructs a new CidrRange whose IP address range is specified by
	 * <code>cidrStr</code>, and whose cluster IP address is
	 * <code>clusterIp</code>.
	 *
	 * @param cidrStr
	 *            A Classless Internet Domain Routing (<a
	 *            href="http://en.wikipedia.org/wiki/Classless_Inter-Domain_Routing">CIDR</a>)
	 *            specifier of the form "W.X.Y.Z/B", where W, X, Y, and Z are
	 *            numeric octets in the closed interval [0,255], and B is the
	 *            prefix length denoting the number IP address high-order bits
	 *            that are significant. All of the 32 - B lower-order bits are
	 *            unfixed. Hence, the size of the range will be 2^(32 - B).
	 *
	 * @param clusterIp
	 *            The "cluster" IP address to which all IP addresses in this
	 *            instance's range will be mapped.
	 *
	 * @throws IllegalArgumentException if <code>cidrStr</code> is an illegal CIDR specifier.
	 */
	public IpAddressRange(String cidrStr, long clusterIp) {
		int index;
		if ((index = cidrStr.indexOf('/')) < 0) {
			throw new IllegalArgumentException(
				"CIDR address string missing '/': " + cidrStr);
		}
		String prefixLenStr = cidrStr.substring(index + 1);
		int prefixLen;
		try {
			prefixLen = Integer.parseInt(prefixLenStr);
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException(
				"Prefix length is not a number: " + prefixLenStr, ex);
		}
		if (prefixLen < 0 || prefixLen > 32) {
			throw new IllegalArgumentException(
				"Prefix length is not in the range [0,32]: " + prefixLen);
		}
		long bitMask = (1L << (32 - prefixLen)) - 1L;
		long cidrVal = IpAddressUtils.ipToLong(cidrStr.substring(0, index));
		if ((cidrVal & bitMask) != 0) {
			throw new IllegalArgumentException(
				"CIDR expression has non-zero low-order bits: " + cidrStr);
		}
		this.startIp = cidrVal;
		this.endIp = cidrVal | bitMask;
		this.clusterIp = clusterIp;
	}

	public IpAddressRange(long startIp, long endIp, long clusterIp) {
		this.startIp = startIp;
		this.endIp = endIp;
		this.clusterIp = clusterIp;
	}

	/**
	 * Returns true if and only if <code>ip</code> is contained
	 * in this instance's IP address range.
	 */
	public boolean containsIp(long ip) {
		return (startIp <= ip && ip <= endIp);
	}

	/**
	 * Returns the first IP address in this instance's IP
	 * address range.
	 */
	public long getStartIp() {
		return startIp;
	}

	/**
	 * Returns the last IP address in this instance's IP
	 * address range.
	 */
	public long getEndIp() {
		return endIp;
	}

	/**
	 * Returns this instance's cluster IP address.
	 */
	public long getClusterIp() {
		return clusterIp;
	}
}
