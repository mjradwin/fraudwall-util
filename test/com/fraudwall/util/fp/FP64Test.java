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
package com.fraudwall.util.fp;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fraudwall.util.AbstractAnchorTest;
import com.fraudwall.util.fnvHash;

/**
 * Tests the {@link FP64} fingerprint implementation.
 *
 * @author Allan Heydon
 */
public class FP64Test extends AbstractAnchorTest {

	// --------------------------------------------------------- FP64()

	public void testNullaryConstructorIsFingerprintOfEmptyString() {
		assertEquals(new FP64(""), new FP64());
	}

	// --------------------------------------------------------- FP64(FP64)

	public void testCopyConstructorThrowsOnNullArgument() {
		try {
			new FP64((FP64) null);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
		}
	}

	public void testCopyConstructor() {
		FP64 fp = new FP64("\u2345testCopyConstructor\u2345");
		assertEquals(fp, new FP64(fp));
	}

	// --------------------------------------------------------- FP64(String)

	public void testStringConstructorIsNullSafe() {
		assertEquals(new FP64(""), new FP64((String) null));
	}

	public void testStringConstructor() {
		final String s = "\u2345testCopyConstructor\u2345";
		assertEquals(new FP64().extend(s), new FP64(s));
	}

	// --------------------------------------------------------- FP64(char[])

	public void testCharArrayConstructorThrowsOnNullArgument() {
		try {
			new FP64((char[]) null);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
		}
	}

	public void testCharArrayConstructor() {
		String s = "\u2345testCharArrayConstructor\u2345";
		assertEquals(new FP64(s), new FP64(s.toCharArray()));
	}

	// --------------------------------------------------------- FP64(char[],int,int)

	public void testCharSubarrayConstructorThrowsOnBadArgs() {
		checkCharSubarrayConstructorThrowsOnBadArgs((char[]) null, 0, 0);
		checkCharSubarrayConstructorThrowsOnBadArgs(new char[5], -1, 0);
		checkCharSubarrayConstructorThrowsOnBadArgs(new char[5], 5, 0);
		checkCharSubarrayConstructorThrowsOnBadArgs(new char[5], 3, -1);
		checkCharSubarrayConstructorThrowsOnBadArgs(new char[5], 3, 3);
	}

	private void checkCharSubarrayConstructorThrowsOnBadArgs(char[] chars, int start, int length) {
		try {
			new FP64(chars, start, length);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
		}
	}

	public void testCharSubarrayConstructor() {
		String s = makeBigString();
		FP64 exp = new FP64(s.substring(10, s.length() - 10));
		FP64 got = new FP64(s.toCharArray(), 10, s.length() - 20);
		assertEquals(exp, got);
	}

	// --------------------------------------------------------- FP64(byte[])

	public void testByteArrayConstructorThrowsOnNullArgument() {
		try {
			new FP64((byte[]) null);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
		}
	}

	public void testByteArrayConstructor() {
		String s = "testByteArrayConstructor";
		FP64 fp = new FP64(s);
		assertEquals(fp, new FP64(s.getBytes()));
	}

	// --------------------------------------------------------- FP64(byte[],int,int)

	public void testByteSubarrayConstructorThrowsOnBadArgs() {
		checkByteSubarrayConstructorThrowsOnBadArgs((byte[]) null, 0, 0);
		checkByteSubarrayConstructorThrowsOnBadArgs(new byte[5], -1, 0);
		checkByteSubarrayConstructorThrowsOnBadArgs(new byte[5], 5, 0);
		checkByteSubarrayConstructorThrowsOnBadArgs(new byte[5], 3, -1);
		checkByteSubarrayConstructorThrowsOnBadArgs(new byte[5], 3, 3);
	}

	private void checkByteSubarrayConstructorThrowsOnBadArgs(byte[] bytes, int start, int length) {
		try {
			new FP64(bytes, start, length);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
		}
	}

	public void testByteSubarrayConstructor() {
		String s = makeBigString(/*bytesOnly=*/ true);
		FP64 exp = new FP64(s.substring(10, s.length() - 10));
		FP64 got = new FP64(s.getBytes(), 10, s.length() - 20);
		assertEquals(exp, got);
	}

	// --------------------------------------------------------- FP64(Reader)

	public void testReaderConstructorThrowsOnNullArgument() throws IOException {
		try {
			new FP64((Reader) null);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
		}
	}

	public void testReaderConstructor() throws Exception {
		String s = "\u2345testReaderConstructor\u2345";
		assertEquals(new FP64(s), new FP64(new StringReader(s)));
	}

	// --------------------------------------------------------- getValue

	public void testGetValue() {
		String s = "\u2345testGetValue\u2345";
		assertEquals(0x2c66d4c7e5d6d341L, new FP64(s).getValue());
	}

	// --------------------------------------------------------- toHexString

	public void testToHexString() {
		String s = "\u2345testToHexString\u2345";
		FP64 fp = new FP64(s);
		assertEquals("6db500d62c240889", fp.toHexString());
	}

	public void testToHexStringPadsToSixteenChars() {
		FP64 fp = new FP64();
		String hexString;
		do {
			hexString = fp.toHexString();
			assertEquals(16, hexString.length());
			fp.extend("abc");
		} while (!hexString.startsWith("0000"));
	}

	// --------------------------------------------------------- extend(String)

	public void testExtendByStringIsNullSafe() {
		assertEquals(new FP64(), new FP64().extend((String) null));
	}

	/**
	 * If "s", "s1", and "s2" are any Strings such that s = s1 + s2, this
	 * tests that "new FP64(s) = new FP64(s1).extend(s2)".
	 */
	public void testExtendByStringEquivalentToFingerprintingConcatenationOfStrings() {
		// form the string "s"
		String s = makeBigString();

		// fingerprint "s"
		FP64 strFP = new FP64(s);

		// check that sFP = new FP64(s1).extend(s2) for all strings s1, s2
		for (int i = 0; i <= s.length(); i++) {
			String s1 = s.substring(0, i);
			String s2 = s.substring(i);
			FP64 s1s2FP = new FP64(s1).extend(s2);
			assertEquals(strFP, s1s2FP);
		}
	}

	// --------------------------------------------------------- extend(char[])

	public void testExtendByCharArrayThrowsOnNullArgument() {
		try {
			new FP64().extend((char[]) null);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
		}
	}

	public void testExtendByCharArray() {
		String s = makeBigString();
		FP64 exp = new FP64(s).extend(s);
		FP64 got = new FP64(s).extend(s.toCharArray());
		assertEquals(exp, got);
	}

	// --------------------------------------------------------- extend(char[],int,int)

	public void testExtendByCharSubarrayThrowsOnBadArgs() {
		checkExtendByCharSubarrayThrowsOnBadArgs((char[]) null, 0, 0);
		checkExtendByCharSubarrayThrowsOnBadArgs(new char[5], -1, 0);
		checkExtendByCharSubarrayThrowsOnBadArgs(new char[5], 5, 0);
		checkExtendByCharSubarrayThrowsOnBadArgs(new char[5], 3, -1);
		checkExtendByCharSubarrayThrowsOnBadArgs(new char[5], 3, 3);
	}

	private void checkExtendByCharSubarrayThrowsOnBadArgs(char[] chars, int start, int length) {
		try {
			new FP64().extend(chars, start, length);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
		}
	}

	public void testExtendByCharSubarray() {
		String s = makeBigString();
		FP64 strFP = new FP64(s);
		char[] chars = s.toCharArray();
		int curr = 10, len = 0;
		FP64 charFP = new FP64(chars, 0, curr);
		while (curr < chars.length) {
			len = Math.min(len, chars.length - curr);
			charFP.extend(chars, curr, len);
			curr += len;
			len++;
		}
		assertEquals(strFP, charFP);
	}

	// --------------------------------------------------------- extend(byte[])

	public void testExtendByByteArrayThrowsOnNullArgument() {
		try {
			new FP64().extend((byte[]) null);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
		}
	}

	public void testExtendByByteArray() {
		String s = makeBigString(/*bytesOnly=*/ true);
		FP64 exp = new FP64(s).extend(s);
		FP64 got = new FP64(s).extend(s.getBytes());
		assertEquals(exp, got);
	}

	// --------------------------------------------------------- extend(byte[],int,int)

	public void testExtendByByteSubarrayThrowsOnBadArgs() {
		checkExtendByByteSubarrayThrowsOnBadArgs((byte[]) null, 0, 0);
		checkExtendByByteSubarrayThrowsOnBadArgs(new byte[5], -1, 0);
		checkExtendByByteSubarrayThrowsOnBadArgs(new byte[5], 5, 0);
		checkExtendByByteSubarrayThrowsOnBadArgs(new byte[5], 3, -1);
		checkExtendByByteSubarrayThrowsOnBadArgs(new byte[5], 3, 3);
	}

	private void checkExtendByByteSubarrayThrowsOnBadArgs(byte[] bytes, int start, int length) {
		try {
			new FP64().extend(bytes, start, length);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
		}
	}

	public void testExtendByByteSubarray() {
		String s = makeBigString();
		FP64 strFP = new FP64(s.getBytes());
		byte[] bytes = s.getBytes();
		int curr = 10, len = 0;
		FP64 charFP = new FP64(bytes, 0, curr);
		while (curr < bytes.length) {
			len = Math.min(len, bytes.length - curr);
			charFP.extend(bytes, curr, len);
			curr += len;
			len++;
		}
		assertEquals(strFP, charFP);
	}

	// --------------------------------------------------------- extend(int)

	public void testExtendByInt() {
		FP64 fp1 = new FP64().extend((byte) 1).extend((byte) 2).extend((byte) 3).extend((byte) 4);
		FP64 fp2 = new FP64().extend((1 << 24) + 2 * (1 << 16) + 3 * (1 << 8) + 4);
		assertEquals(fp1, fp2);
	}

	// --------------------------------------------------------- extend(long)

	public void testExtendByLong() {
		FP64 fp1 = new FP64().extend((byte) 255).extend((byte) 2).extend((byte) 253).extend((byte) 4);
		fp1.extend((byte) 5).extend((byte) 6).extend((byte) 7).extend((byte) 8);
		FP64 fp2 = new FP64().extend(0xff02fd04).extend(0x05060708);
		assertEquals(fp1, fp2);
	}

	// --------------------------------------------------------- extend(char)

	public void testExtendByChar() {
		String s = makeBigString();
		char[] chars = s.toCharArray();
		FP64 strFP = new FP64(s);
		FP64 byCharFP = new FP64();
		for (int i = 0; i < chars.length; i++) {
			byCharFP.extend(chars[i]);
		}
		assertEquals(strFP, byCharFP);
	}

	// --------------------------------------------------------- extend(byte)

	public void testExtendByByte() {
		String s = makeBigString(/*bytesOnly=*/ true);
		byte[] bytes = s.getBytes();
		assertEquals(s.length(), bytes.length);
		FP64 strFP = new FP64(s);
		FP64 byByteFP = new FP64();
		for (int i = 0; i < bytes.length; i++) {
			byByteFP.extend(bytes[i]);
		}
		assertEquals(strFP, byByteFP);
	}

	// --------------------------------------------------------- extend(Reader)

	public void testExtendByReaderThrowsOnNullArgument() throws IOException {
		try {
			new FP64().extend((Reader) null);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
		}
	}

	public void testExtendByReader() throws IOException {
		String s = makeBigString();
		FP64 exp = new FP64(s).extend(s);
		FP64 got = new FP64(s).extend(new StringReader(s));
		assertEquals(exp, got);
	}

	// --------------------------------------------------------- fp(String)

	public void testFpOnStringIsNullSafeAndReturns0() {
		assertEquals(0L, FP64.fp((String) null));
	}

	public void testFpOnEmptyStringAndReturns0() {
		assertEquals(0L, FP64.fp(""));
	}

	public void testFpOnString() {
		final String s = "\u2345testFpOfString\u2345";
		assertEquals(new FP64(s).getValue(), FP64.fp(s));
	}

	// --------------------------------------------------------- equals

	public void testEqualsReturnsTrueForEqualRefs() {
		FP64 fp = new FP64();
		assertTrue(fp.equals(fp));
	}

	public void testEqualsReturnsFalseForArgumentOfIncorrectType() {
		String s = "testEqualsReturnsFalseForArgumentOfIncorrectType";
		assertFalse(new FP64(s).equals(s));
	}

	public void testEqualsReturnsFalseForFPsOfDifferentBytes() {
		String s1 = "testEqualsReturnsFalseForFPsOfDifferentBytes";
		String s2 = s1 + "-v2";
		assertFalse(new FP64(s1).equals(new FP64(s2)));
	}

	public void testEqualsReturnsTrueForDifferentFPsOfIdenticalBytes() {
		String s = "testEqualsReturnsTrueForDifferentFPsOfIdenticalBytes";
		FP64 fp1 = new FP64(s);
		FP64 fp2 = new FP64(s);
		assertNotSame(fp1, fp2);
		assertTrue(fp1.equals(fp2));
	}

	// --------------------------------------------------------- hashcode

	public void testHashcodeAndEquals() {
		// populate map with FPs mapped to strings that produced them
		final int N = 1000;
		Map<FP64, String> map = new HashMap<FP64, String>(N);
		final String suffix = makeBigString();
		for (int i = 0; i < N; i++) {
			String s = Integer.toString(i) + " - " + suffix;
			FP64 fp = new FP64(s);
			String gotS = map.put(fp, s);
			// assert that no two distinct strings have the same FP
			assertNull("Diff strings have identical FP: '" + s + "' and '" + gotS + "'", gotS);
		}
		assertEquals(N, map.size());

		// check ability to look up values in the map by FP
		for (String s: map.values()) {
			FP64 sFP = new FP64(s);
			Object val = map.get(sFP);
			assertEquals(s, val);
		}
	}

	// --------------------------------------------------------- sanity tests

	public void testFingprintingOfSimilarStrings() {
		Set<FP64> s = new HashSet<FP64>();
		fp(s, "The quick brown fox");
		fp(s, "The Quick brown fox");
		fp(s, "The quick crown fox");
		fp(s, "The quick brown box");
		fp(s, "The quick brown foxx");
		fp(s, "");
		fp(s, "a");
		fp(s, "aa");
		fp(s, "aaa");
		fp(s, "aaaa");
		fp(s, "b");
		fp(s, "bb");
		fp(s, "bbb");
		fp(s, "bbbb");
	}

	private void fp(Set<FP64> seen, String s) {
		FP64 fp = new FP64(s);
		//System.out.println("FP64(" + s + ") --> " + fp.toHexString());
		assertFalse(seen.contains(fp));
		seen.add(fp);
	}

	// --------------------------------------------------------- private helpers

	private String makeBigString() {
		return makeBigString(/*bytesOnly=*/ false);
	}

	private String makeBigString(boolean bytesOnly) {
		String x = "Hello, Fingerprint!";
		if (!bytesOnly) {
			x = "\u2345" + x + "\u3456";
		}
		StringBuilder res = new StringBuilder(x.length() * 10);
		for (int i = 0; i < 10; i++) {
			res.append(x);
		}
		return res.toString();
	}

	public void XtestPerfOfFP64vsFnvHash() {
		checkPerfOfFP64vsFnvHash(5, 10000000, false);
		checkPerfOfFP64vsFnvHash(5, 10000000, true);
	}

	private void checkPerfOfFP64vsFnvHash(int rounds, int n, boolean checkForCollisions) {
		final String fileName = "events.1235767953.csv";
		final Set<Long> set = new HashSet<Long>(checkForCollisions ? n : 0);
		for (int round = 1; round <= rounds; round++) {
			System.err.println("Round " + round);
			System.err.println();
			long start = System.currentTimeMillis();
			for (int i = 0; i < n; i++) {
				long hash = fnvHash.fnv64aHash(fileName + "." + i);
				if (checkForCollisions && !set.add(hash)) {
					System.err.println("Got duplicate hash value: " + hash);
				}
			}
			System.err.println("fnvHash time = " + (System.currentTimeMillis() - start) + " ms");
			set.clear();
			start = System.currentTimeMillis();
			for (int i = 0; i < n; i++) {
				long hash = new FP64(fileName).extend('.').extend(i).getValue();
				if (checkForCollisions && !set.add(hash)) {
					System.err.println("Got duplicate hash value: " + hash);
				}
			}
			System.err.println("FP64 full time = " + (System.currentTimeMillis() - start) + " ms");
			set.clear();
			start = System.currentTimeMillis();
			FP64 fp = new FP64(fileName);
			for (int i = 0; i < n; i++) {
				long hash = new FP64(fp).extend('.').extend(i).getValue();
				if (checkForCollisions && !set.add(hash)) {
					System.err.println("Got duplicate hash value: " + hash);
				}
			}
			System.err.println("FP64 inc time = " + (System.currentTimeMillis() - start) + " ms");
			System.err.println();
			set.clear();
		}
	}
}
