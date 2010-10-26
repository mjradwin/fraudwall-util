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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Fowler/Noll/Vo (FNV) hash function.<p>
 *
 * [Rewritten from the C implementation containing the following comment.]<p>
 *
 * The basis of this hash algorithm was taken from an idea sent
 * as reviewer comments to the IEEE POSIX P1003.2 committee by:
 * <pre>
 *      Phong Vo (http://www.research.att.com/info/kpv/)
 *      Glenn Fowler (http://www.research.att.com/~gsf/)
 * </pre>
 * In a subsequent ballot round:
 * <pre>
 *      Landon Curt Noll (http://www.isthe.com/chongo/)
 * </pre>
 * improved on their algorithm.  Some people tried this hash
 * and found that it worked rather well.  In an EMail message
 * to Landon, they named it the ``Fowler/Noll/Vo'' or FNV hash.<p>
 *
 * FNV hashes are designed to be fast while maintaining a low
 * collision rate. The FNV speed allows one to quickly hash lots
 * of data while maintaining a reasonable collision rate.  See:
 * <pre>
 *      http://www.isthe.com/chongo/tech/comp/fnv/index.html
 * </pre>
 * for more details as well as other forms of the FNV hash.
 *
 * @author Dan Arias
 */
public class fnvHash {

	// ============================ 32-bit FNV-1a hash ============================

	private static final long FNV1_32A_INIT = (0x811c9dc5L);

	/**
	 * Returns the 32-bit hash of the given string, or 0L if <code>data</code>
	 * is null or empty.
	 */
	public static long fnv32aHash(String data) {
		return fnv32aHash(data, FNV1_32A_INIT);
	}

	private static long fnv32aHash(String data, long hval) {
		if (StringUtils.isEmpty(data)) {
			//This is a deviation from standard fnv.  The reasons, for better or worse,
			//are to equate empty string and null, and to give a human-obvious hash value for both.
			//This makes hash columns non-null in the database, simplifies joins, and
			//aides analist understanding downstream.
			return 0;
		}
		for (int i = 0; i < data.length(); i++) {
			hval ^= data.charAt(i);
			hval += (hval << 1) + (hval << 4) + (hval << 7) + (hval << 8) + (hval << 24);
		}
		return hval & 0x00000000ffffffffL;
	}

	/**
	 * Returns the 32-bit hash of the given bytes, or 0L if <code>data</code>
	 * is null or empty.
	 */
	public static long fnv32aHash(byte [] data) {
		return fnv32aHash(data, FNV1_32A_INIT);
	}

	private static long fnv32aHash(byte [] data, long hval) {
		if (ArrayUtils.isEmpty(data)) {
			//This is a deviation from standard fnv.  The reasons, for better or worse,
			//are to equate empty string and null, and to give a human-obvious hash value for both.
			//This makes hash columns non-null in the database, simplifies joins, and
			//aides analyst understanding downstream.
			return 0;
		}
		for (int i = 0; i < data.length; i++) {
			hval ^= data[i];
			hval += (hval << 1) + (hval << 4) + (hval << 7) + (hval << 8) + (hval << 24);
		}
		return hval & 0x00000000ffffffffL;
	}

	// ============================ 64-bit FNV-1a hash ============================

	private static final long FNV1_64A_INIT = (0xcbf29ce484222325L);

	/**
	 * Returns the 64-bit hash of the given string, or 0L if <code>data</code>
	 * is null or empty.
	 */
	public static long fnv64aHash(String data) {
		return fnv64aHash(data, FNV1_64A_INIT);
	}

	private static long fnv64aHash(String data, long hval) {
		if (StringUtils.isEmpty(data)) {
			//This is a deviation from standard fnv.  The reasons, for better or worse,
			//are to equate empty string and null, and to give a human-obvious hash value for both.
			//This makes hash columns non-null in the database, simplifies joins, and
			//aides analyst understanding downstream.
			return 0;
		}
		for (int i = 0; i < data.length(); i++) {
			hval ^= data.charAt(i);
			hval += (hval << 1) + (hval << 4) + (hval << 5) + (hval << 7) + (hval << 8) + (hval << 40);
		}
		return hval;
	}

	/**
	 * Returns the 64-bit hash of the given bytes, or 0L if <code>data</code>
	 * is null or empty.
	 */
	public static long fnv64aHash(byte[] data) {
		return fnv64aHash(data, FNV1_64A_INIT);
	}

	private static long fnv64aHash(byte[] data, long hval) {
		if (ArrayUtils.isEmpty(data)) {
			//This is a deviation from standard fnv.  The reasons, for better or worse,
			//are to equate empty string and null, and to give a human-obvious hash value for both.
			//This makes hash columns non-null in the database, simplifies joins, and
			//aides analyst understanding downstream.
			return 0;
		}
		for (int i = 0; i < data.length; i++) {
			hval ^= data[i];
			hval += (hval << 1) + (hval << 4) + (hval << 5) + (hval << 7) + (hval << 8) + (hval << 40);
		}
		return hval;
	}

	public static void main(String[] args) {
		for (String arg : args)
			System.out.println(arg + "=" + fnvHash.fnv64aHash(arg));
	}
}
