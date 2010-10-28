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

public class StringUtilsTest extends AbstractAnchorTest {

	/** {@link StringUtils#isUrlEncoded(String)} ---------------------------------------------------------------- */

	public void testIsUrlEncodedThrowsForNullArgument() {
		try {
			StringUtils.isUrlEncoded(null);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			assertIsNotNullException("text", ex);
		}
	}

	public void testIsUrlEncodedReturnsFalseForEmptyOrBlankString() {
		checkIsUrlEncodedOn(false, "");
		checkIsUrlEncodedOn(false, "  ");
		checkIsUrlEncodedOn(false, "\t \t");
	}

	public void testIsUrlEncodedReturnsCorrectResult() {
		checkIsUrlEncodedOn(false, "23");
		checkIsUrlEncodedOn(false, "Ovens Auditorium , Charlotte , NC");
		checkIsUrlEncodedOn(true, "lawrence p+ray+black++inventor");
		checkIsUrlEncodedOn(true, "download+movies+for+free");
		checkIsUrlEncodedOn(true, "%09This%12is%43a%54test%C2%A2");
		checkIsUrlEncodedOn(true, "www%2egoogle%2ecom");
		checkIsUrlEncodedOn(true, ".you%20tube.com");
		checkIsUrlEncodedOn(true, "ohio%20attorney%20general's%20office");
		checkIsUrlEncodedOn(true, "http://www.environmentaldefense.org/page.cfm%3Ftagid%3D1344");
	}

	private void checkIsUrlEncodedOn(boolean expResult, String queryText) {
		assertEquals(expResult, StringUtils.isUrlEncoded(queryText));
	}

	/** {@link StringUtils#decodeString(String)} ---------------------------------------------------------------- */

	public void testDecodeStringConvertsEscapesToASCII() {
		checkDecodeString("http://anchorintelligence.com", "http%3A%2F%2Fanchorintelligence%2Ecom");
		checkDecodeString("http://anchorintelligence.com/", "http%3A%2F%2Fanchorintelligence%2Ecom%2F");
		checkDecodeString("12.199.87.54", "12%2E199%2E87%2E54");
	}

	public void testDecodeStringDoesUTF8DecodingOfEscapedByteSequences() {
		checkDecodeString("foo\u0080bar", "foo%C2%80bar"); // min 2-byte value
		checkDecodeString("foo\u07FFbar", "foo%DF%BFbar"); // max 2-byte value
		checkDecodeString("foo.bar", "foo%C0%AEbar"); // 2-byte encoding of 7-bit ASCII char (.)
		checkDecodeString("foo\u00A2bar", "foo%C2%A2bar"); // cent sign
		checkDecodeString("foo\u05D0bar", "foo%D7%90bar"); // aleph
	}

	private void checkDecodeString(String exp, String input) {
		assertEquals(exp, StringUtils.decodeString(input));
	}

	public void testDecodeStringDoesNotConvertPlusCharacters() {
		assertEquals("a+b+c", StringUtils.decodeString("a+b+c"));
	}

	/** {@link StringUtils#decodeURL(String)} ------------------------------------------------------------------- */

	public void testDecodeURLReturnsSameStringWhenNoConversionIsNecessary() {
		String s = "This string does not contain any plus or percent characters";
		assertSame(s, StringUtils.decodeURL(s));
	}

	public void testDecodeURLReplacesPlusesWithSpaces() {
		checkDecodeURL("this is a test", "this+is+a+test");
		checkDecodeURL(" this is a test ", "+this+is+a+test+");
		checkDecodeURL("  this is   a test  ", "++this+is+++a+test++");
	}

	public void testDecodeURLReturnsCorrectValueFor1ByteUTF8Characters() {
		checkDecodeURL("http://anchorintelligence.com", "http%3A%2F%2Fanchorintelligence%2Ecom");
		checkDecodeURL("http://anchorintelligence.com/", "http%3A%2F%2Fanchorintelligence%2Ecom%2F");
		checkDecodeURL("12.199.87.54", "12%2E199%2E87%2E54");
	}

	public void testDecodeURLReturnsCorrectValueFor2ByteUTF8Characters() {
		checkDecodeURL("foo\u0080bar", "foo%C2%80bar"); // min 2-byte value
		checkDecodeURL("foo\u07FFbar", "foo%DF%BFbar"); // max 2-byte value
		checkDecodeURL("foo.bar", "foo%C0%AEbar"); // 2-byte encoding of 7-bit ASCII char (.)
		checkDecodeURL("foo\u00A2bar", "foo%C2%A2bar"); // cent sign
		checkDecodeURL("foo\u05D0bar", "foo%D7%90bar"); // aleph
	}

	public void testDecodeURLReturnsCorrectValueFor3ByteUTF8Characters() {
		checkDecodeURL("foo\u0800bar", "foo%E0%A0%80bar"); // min 3-byte value
		checkDecodeURL("foo\uFFFFbar", "foo%EF%BF%BFbar"); // max 3-byte value
		checkDecodeURL("foo.bar", "foo%E0%80%AEbar"); // 3-byte encoding of 7-bit ASCII char (.)
		checkDecodeURL("foo\u00A2bar", "foo%E0%82%A2bar"); // 3-byte encoding of cent sign
		checkDecodeURL("foo\uD9ABbar", "foo%ED%A6%ABbar"); // char in range D800-DFFF disallowed by Unicode
	}

	public void testDecodeURLDecodesMultipleConsecutiveUTF8EncodedChars() {
		checkDecodeURL("foo://\u05D0bar", "foo%3A%2F%2F%D7%90bar");
		checkDecodeURL("foo\uFFFF\u00A2bar", "foo%EF%BF%BF%C2%A2bar");
		checkDecodeURL("foo \u00A2\uD9AB bar", "foo+%C2%A2%ED%A6%AB+bar"); // with '+'s
	}

	public void testDecodeURLSkipsUnfinished2ByteUTF8EncodedChars() {
		checkDecodeURL(":foo\uFFFDbar:", "%3Afoo%C0bar%3A");
		checkDecodeURL(":foo.\uFFFDbar:", "%3Afoo%2E%C0bar%3A");
		checkDecodeURL(":foo.\u00A2\uFFFDbar:", "%3Afoo%2E%C2%A2%C3bar%3A");
	}

	public void testDecodeURLSkipsUnfinished3ByteUTF8EncodedChars() {
		checkDecodeURL(":foo.\u00A2\uFFFDbar:", "%3Afoo%2E%C2%A2%E1bar%3A");
		checkDecodeURL(":foo.\u00A2\uFFFDbar:", "%3Afoo%2E%C2%A2%E1%BFbar%3A");
		checkDecodeURL(":foo\uFFFDbar:", "%3Afoo%E1bar%3A");
		checkDecodeURL(":foo\uFFFDbar:", "%3Afoo%E1%BFbar%3A");
	}

	public void testDecodeURLSkipsUnfinished2ByteUTF8EncodedCharsAtEndOfString() {
		checkDecodeURL("\uFFFD", "%C0");
		checkDecodeURL(".\uFFFD", "%2E%C0");
		checkDecodeURL(".\u00A2\uFFFD", "%2E%C2%A2%C3");
	}

	public void testDecodeURLSkipsUnfinished3ByteUTF8EncodedCharsAtEndOfString() {
		checkDecodeURL("\uFFFD", "%E1");
		checkDecodeURL(".\u00A2\uFFFD", "%2E%C2%A2%E1");
		checkDecodeURL("\uFFFD", "%E1%BF");
		checkDecodeURL(".\u00A2\uFFFD", "%2E%C2%A2%E1%BF");
	}

	public void testDecodeURLSkipsUTF8EncodedCharsWithIllegalLeadByte() {
		for (int i = 0x80; i < 0xC0; i++) {
			checkDecodeURL(":foo.\uFFFD.bar:", String.format("%%3Afoo%%2E%%%02X%%2Ebar%%3A", i));
		}
		for (int i = 0xF0; i < 0x100; i++) {
			checkDecodeURL(":foo/\uFFFD/bar:", String.format("%%3Afoo%%2F%%%02X%%2Fbar%%3A", i));
		}
	}

	public void testDecodeURLSkipsUTF8EncodedCharsWithIllegal2ndByteOf2ByteSequence() {
		for (int b1 = 0xC0; b1 < 0xE0; b1++) {
			for (int b2 = 0x00; b2 < 0x80; b2++) {
				char c = (char) b2;
				checkDecodeURL(":foo.\uFFFD" + c + ".bar:",
					String.format("%%3Afoo%%2E%%%02X%%%02X%%2Ebar%%3A", b1, b2));
			}
			for (int b2 = 0xC0; b2 < 0x100; b2++) {
				checkDecodeURL(":foo/\uFFFD\uFFFD/bar:",
					String.format("%%3Afoo%%2F%%%02X%%%02X%%2Fbar%%3A", b1, b2));
			}
		}
	}

	public void testDecodeURLSkipsUTF8EncodedCharsWithIllegal2ndByteOf3ByteSequence() {
		for (int b1 = 0xE0; b1 < 0xF0; b1++) {
			for (int b2 = 0x00; b2 < 0x80; b2++) {
				char c = (char) b2;
				checkDecodeURL(":foo.\uFFFD" + c + ".bar:",
					String.format("%%3Afoo%%2E%%%02X%%%02X%%2Ebar%%3A", b1, b2));
			}
			for (int b2 = 0xC0; b2 < 0x100; b2++) {
				checkDecodeURL(":foo/\uFFFD\uFFFD/bar:",
					String.format("%%3Afoo%%2F%%%02X%%%02X%%2Fbar%%3A", b1, b2));
			}
		}
	}

	public void testDecodeURLSkipsUTF8EncodedCharsWithIllegal3rdByteOf3ByteSequence() {
		for (int b1 = 0xE0; b1 < 0xF0; b1++) {
			for (int b3 = 0x00; b3 < 0x80; b3++) {
				char c = (char) b3;
				checkDecodeURL(":foo.\uFFFD" + c + ".bar:",
					String.format("%%3Afoo%%2E%%%02X%%81%%%02X%%2Ebar%%3A", b1, b3));
			}
			for (int b3 = 0xC0; b3 < 0x100; b3++) {
				checkDecodeURL(":foo/\uFFFD\uFFFD/bar:",
					String.format("%%3Afoo%%2F%%%02X%%81%%%02X%%2Fbar%%3A", b1, b3));
			}
		}
	}

	private void checkDecodeURL(String expOutput, String input) {
		String res = StringUtils.decodeURL(input);
		if (!res.equals(expOutput)) {
			System.err.println("Input:    " + input);
			writeHex("Expected:", expOutput);
			writeHex("Got:     ", res);
		}
		assertEquals(expOutput, res);
	}

	public void testDecodeURLThrowsIllegalArgumentExceptionIfPercentNotFollowedByTwoHexCharacters() {
		checkDecodeURLThrowsOn("no good: %2xa", "2x");
		checkDecodeURLThrowsOn("no good: %g2f", "g2");
		checkDecodeURLThrowsOn("no good: %5", "5");
		checkDecodeURLThrowsOn("no good: %", "");
	}

	private void checkDecodeURLThrowsOn(String s, String badChars) {
		try {
			StringUtils.decodeURL(s);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			String errMsg = (badChars.length() < 2)
				? "Incomplete trailing escape (%) pattern"
				: "Illegal hex characters in escape (%) pattern - " +
				"For input string: \"" + badChars + "\"";
			assertEquals(errMsg, ex.getMessage());
		}
	}

	/** {@link StringUtils#decodeUTF8(byte[], int)} ------------------------------------------------------------- */

	public void testDecodeUTF8Decodes1ByteChars() {
		checkDecodeUTF8(".", 0x2E);
		checkDecodeUTF8("://", 0x3A, 0x2F, 0x2F);
	}

	public void testDecodeUTF8Decodes2ByteChars() {
		checkDecodeUTF8("\u0080", 0xC2, 0x80); // min 2-byte value
		checkDecodeUTF8("\u07FF", 0xDF, 0xBF); // max 2-byte value
		checkDecodeUTF8(".",      0xC0, 0xAE); // 2-byte encoding of 7-bit ASCII char (.)
		checkDecodeUTF8("\u00A2", 0xC2, 0xA2); // cent sign
		checkDecodeUTF8("\u05D0", 0xD7, 0x90); // aleph
	}

	public void testDecodeUTF8Decodes3ByteChars() {
		checkDecodeUTF8("\u0800", 0xE0, 0xA0, 0x80); // min 3-byte value
		checkDecodeUTF8("\uFFFF", 0xEF, 0xBF, 0xBF); // max 3-byte value
		checkDecodeUTF8(".",      0xE0, 0x80, 0xAE); // 3-byte encoding of 7-bit ASCII char (.)
		checkDecodeUTF8("\u00A2", 0xE0, 0x82, 0xA2); // 3-byte encoding of cent sign
		checkDecodeUTF8("\uD9AB", 0xED, 0xA6, 0xAB); // char in range D800-DFFF disallowed by Unicode
	}

	public void testDecodeUTF8DecodesMultipleConsecutiveUTF8EncodedChars() {
		checkDecodeUTF8(":\u05D0//", 0x3A, 0xD7, 0x90, 0x2F, 0x2F);
		checkDecodeUTF8("\uFFFF\u00A2", 0xEF, 0xBF, 0xBF, 0xC2, 0xA2);
		checkDecodeUTF8("\u00A2\uD9AB", 0xC2, 0xA2, 0xED, 0xA6, 0xAB);
		checkDecodeUTF8(":\u00A2/\uD9AB/", 0x3A, 0xC2, 0xA2, 0x2F, 0xED, 0xA6, 0xAB, 0x2F);
	}

	public void testDecodeUTF8HandlesUnfinished2ByteUTF8ByteSequences() {
		checkDecodeUTF8("\uFFFD", 0xC0);
		checkDecodeUTF8(".\uFFFD", 0x2E, 0xC0);
		checkDecodeUTF8(".\u00A2\uFFFD", 0x2E, 0xC2, 0xA2, 0xC3);
	}

	public void testDecodeUTF8HandlesUnfinished3ByteUTF8ByteSequences() {
		checkDecodeUTF8(".\u00A2\uFFFD", 0x2E, 0xC2, 0xA2, 0xE1);
		checkDecodeUTF8(".\u00A2\uFFFD", 0x2E, 0xC2, 0xA2, 0xE1, 0xBF);
		checkDecodeUTF8("\uFFFD", 0xE1);
		checkDecodeUTF8("\uFFFD", 0xE1, 0xBF);
	}

	public void testDecodeUTF8HandlesUnfinished2ByteUTF8ByteSequencesAtEndOfArray() {
		checkDecodeUTF8(false, "\uFFFD", 0xC0);
		checkDecodeUTF8(false, ".\uFFFD", 0x2E, 0xC0);
		checkDecodeUTF8(false, ".\u00A2\uFFFD", 0x2E, 0xC2, 0xA2, 0xC3);
	}

	public void testDecodeUTF8HandlesUnfinished3ByteUTF8ByteSequencesAtEndOfArray() {
		checkDecodeUTF8(false, ".\u00A2\uFFFD", 0x2E, 0xC2, 0xA2, 0xE1);
		checkDecodeUTF8(false, ".\u00A2\uFFFD", 0x2E, 0xC2, 0xA2, 0xE1, 0xBF);
		checkDecodeUTF8(false, "\uFFFD", 0xE1);
		checkDecodeUTF8(false, "\uFFFD", 0xE1, 0xBF);
	}

	public void testDecodeUTF8HandlesIllegalLeadByte() {
		for (int b1 = 0x80; b1 < 0xC0; b1++) {
			checkDecodeUTF8(".\uFFFD.", 0x2E, b1, 0x2E);
		}
		for (int b1 = 0xF0; b1 < 0x100; b1++) {
			checkDecodeUTF8("/\uFFFD/", 0x2F, b1, 0x2F);
		}
		checkDecodeUTF8(".\uFFFD.\uFFFD.", 0x2E, 0x90, 0x2E, 0x91, 0x2E);
		checkDecodeUTF8(".\uFFFD\u00A2\uFFFD.", 0x2E, 0xF0, 0xC2, 0xA2, 0xF1, 0x2E);
	}

	public void testDecodeUTF8HandlesIllegal2ndByteOf2ByteSequence() {
		for (int b1 = 0xC0; b1 < 0xE0; b1++) {
			for (int b2 = 0x00; b2 < 0x80; b2++) {
				char c2 = (char) b2;
				checkDecodeUTF8(".\uFFFD" + c2 + ".", 0x2E, b1, b2, 0x2E);
			}
			for (int b2 = 0xC0; b2 < 0x100; b2++) {
				checkDecodeUTF8(".\uFFFD\uFFFD.", 0x2E, b1, b2, 0x2E);
			}
		}
	}

	public void testDecodeUTF8ThrowsForIllegal2ndByteOf3ByteSequence() {
		for (int b1 = 0xE0; b1 < 0xF0; b1++) {
			for (int b2 = 0x00; b2 < 0x80; b2++) {
				char c2 = (char) b2;
				checkDecodeUTF8(".\uFFFD" + c2 + ".", 0x2E, b1, b2, 0x2E);
			}
			for (int b2 = 0xC0; b2 < 0x100; b2++) {
				checkDecodeUTF8(".\uFFFD\uFFFD.", 0x2E, b1, b2, 0x2E);
			}
		}
	}

	public void testDecodeUTF8ThrowsForIllegal3rdByteOf3ByteSequence() {
		for (int b1 = 0xE0; b1 < 0xF0; b1++) {
			for (int b3 = 0x00; b3 < 0x80; b3++) {
				char c3 = (char) b3;
				checkDecodeUTF8(".\uFFFD" + c3 + ".", 0x2E, b1, 0x80, b3, 0x2E);
			}
			for (int b3 = 0xC0; b3 < 0x100; b3++) {
				checkDecodeUTF8(".\uFFFD\uFFFD.", 0x2E, b1, 0x81, b3, 0x2E);
			}
		}
	}

	private void checkDecodeUTF8(String expOutput, int... vals) {
		checkDecodeUTF8(true, expOutput, vals);
	}

	/**
	 * @param extendVals
	 * copy bytes to longer array to check that bytes past end position are ignored
	 */
	private void checkDecodeUTF8(boolean extendVals, String expOutput, int... vals) {
		byte[] input = new byte[vals.length * (extendVals ? 2 : 1)];
		copyIntsToBytes(vals, input, 0);
		if (extendVals) {
			copyIntsToBytes(vals, input, vals.length);
		}
		checkDecodeUTF8(expOutput, input, vals);
	}

	private void checkDecodeUTF8(String expOutput, byte[] input, int... vals) {
		String res = StringUtils.decodeUTF8(input, vals.length);
		if (!res.equals(expOutput)) {
			writeHex("Input:   ", new String(input));
			writeHex("Expected:", expOutput);
			writeHex("Got:     ", res);
		}
		assertEquals(expOutput, res);
	}

	private void writeHex(String message, String s) {
		System.err.print(message + " ");
		for (int i = 0; i < s.length(); i++) {
			System.err.print(String.format("%02X ", s.codePointAt(i)));
		}
		System.err.println();
	}

	private void copyIntsToBytes(int[] ints, byte[] bytes, int destIx) {
		for (int i = 0, j = destIx; i < ints.length; i++,j++) {
			bytes[j] = (byte) ints[i];
		}
	}

	/** {@link StringUtils#asDatabaseID(String)} ---------------------------------------------------------------- */

	public void testAsDatabaseIDReturnsCorrectDatabaseIdentifier() {
		assertEquals("query_string", StringUtils.asDatabaseID("QueryString"));
		assertEquals("this_is_a_test", StringUtils.asDatabaseID("ThisIsATest"));
		assertEquals("my_lru_cache", StringUtils.asDatabaseID("MyLruCache"));
		assertEquals("my_lru_cache", StringUtils.asDatabaseID("myLruCache"));
	}

	/** {@link StringUtils#convertCamelCaseToCharSeparatedString(String, char)} --------------------------------- */

	public void testConvertCamelCaseToCharSeparatedString() {
		assertEquals("query_string", StringUtils.convertCamelCaseToCharSeparatedString("QueryString", '_'));
		assertEquals("this_is_a_test", StringUtils.convertCamelCaseToCharSeparatedString("ThisIsATest", '_'));
		assertEquals("my lru cache", StringUtils.convertCamelCaseToCharSeparatedString("MyLruCache", ' '));
		assertEquals("my lru cache", StringUtils.convertCamelCaseToCharSeparatedString("myLruCache", ' '));
	}

	/** {@link StringUtils#toCamelCase(String)} ----------------------------------------------------------------- */

	public void testToCamelCaseReturnsCorrectCamelCaseString() {
		assertEquals("QueryString", StringUtils.toCamelCase("query_string"));
		assertEquals("QueryString", StringUtils.toCamelCase("queryString"));
		assertEquals("ThisIsATest", StringUtils.toCamelCase("this_is_a_test"));
		assertEquals("MyLruCache", StringUtils.toCamelCase("my_lru_cache"));
	}

	/** {@link StringUtils#numComponents(String)} --------------------------------------------------------------- */

	public void testNumComponentsReturnsZeroIfArgumentIsNull() {
		assertEquals(0, StringUtils.numComponents(null));
	}

	public void testNumComponentsReturnsCorrectResults() {
		assertEquals(1, StringUtils.numComponents(""));
		assertEquals(1, StringUtils.numComponents("  "));
		assertEquals(1, StringUtils.numComponents("abc"));
		assertEquals(2, StringUtils.numComponents("ab.c"));
		assertEquals(2, StringUtils.numComponents("ab."));
		assertEquals(2, StringUtils.numComponents(".c"));
		assertEquals(3, StringUtils.numComponents("a.b.c"));
		assertEquals(3, StringUtils.numComponents(".b."));
	}

	/** {@link StringUtils#firstComponent(String)} -------------------------------------------------------------- */

	public void testFirstComponentReturnsTheEntireStringIfItContainsNoDots() {
		assertEquals("string_with_underscores", StringUtils.firstComponent("string_with_underscores"));
		assertEquals("string-with-hyphens", StringUtils.firstComponent("string-with-hyphens"));
		assertEquals("string/with/slashes", StringUtils.firstComponent("string/with/slashes"));
	}

	public void testFirstComponentReturnsStringBeforeFirstDot() {
		assertEquals("", StringUtils.firstComponent(".string.startinging.in.dot."));
		assertEquals("foo", StringUtils.firstComponent("foo."));
		assertEquals("mithra", StringUtils.firstComponent("mithra.anchorintelligence.com"));
	}

	/** {@link StringUtils#lastComponent(String)} --------------------------------------------------------------- */

	public void testLastComponentReturnsTheEntireStringIfItContainsNoDots() {
		assertEquals("string_with_underscores", StringUtils.lastComponent("string_with_underscores"));
		assertEquals("string-with-hyphens", StringUtils.lastComponent("string-with-hyphens"));
		assertEquals("string/with/slashes", StringUtils.lastComponent("string/with/slashes"));
	}

	public void testLastComponentReturnsStringAfterLastDot() {
		assertEquals("", StringUtils.lastComponent("string.ending.in.dot."));
		assertEquals("gzip", StringUtils.lastComponent(".gzip"));
		assertEquals("util", StringUtils.lastComponent("com.fraudwall.util"));
	}

	/** {@link StringUtils#lastComponents(String, int)} --------------------------------------------------------- */

	public void testLastComponentsThrowsIfNIsNotStrictlyPositive() {
		checkLastComponentsThrowsIfNIsNotStrictlyPositive("a.b.c", 0);
	}

	private void checkLastComponentsThrowsIfNIsNotStrictlyPositive(String s, int n) {
		try {
			StringUtils.lastComponents(s, n);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			assertEquals("Number of components 'n' must be strictly positive.", ex.getMessage());
		}
	}

	public void testLastComponentsReturnsCorrectResults() {
		assertEquals("foo", StringUtils.lastComponents("foo", 1));
		assertEquals("foo", StringUtils.lastComponents("foo", 2));
		assertEquals("c", StringUtils.lastComponents("a.b.c", 1));
		assertEquals("b.c", StringUtils.lastComponents("a.b.c", 2));
		assertEquals("a.b.c", StringUtils.lastComponents("a.b.c", 3));
		assertEquals("a.b.c", StringUtils.lastComponents("a.b.c", 4));
		assertEquals("", StringUtils.lastComponents("a.b.", 1));
		assertEquals("b.", StringUtils.lastComponents(".b.", 2));
		assertEquals(".b.", StringUtils.lastComponents(".b.", 3));
		assertEquals(".b.", StringUtils.lastComponents(".b.", 5));
	}

	/** {@link StringUtils#chopLastComponent(String)} ----------------------------------------------------------- */

	public void testChopLastComponentReturnsTheEntireStringIfItContainsNoDots() {
		assertEquals("string_with_underscores", StringUtils.chopLastComponent("string_with_underscores"));
		assertEquals("string-with-hyphens", StringUtils.chopLastComponent("string-with-hyphens"));
		assertEquals("string/with/slashes", StringUtils.chopLastComponent("string/with/slashes"));
	}

	public void testChopLastComponentReturnsPrefixBeforeLastDot() {
		assertEquals("string.ending.in.dot", StringUtils.chopLastComponent("string.ending.in.dot."));
		assertEquals("", StringUtils.chopLastComponent(".gzip"));
		assertEquals("com.fraudwall", StringUtils.chopLastComponent("com.fraudwall.util"));
	}

	/** {@link StringUtils#chopFirstComponent(String)} ---------------------------------------------------------- */

	public void testChopFirstComponentReturnsTheEntireStringIfItContainsNoDots() {
		assertEquals("string_with_underscores", StringUtils.chopFirstComponent("string_with_underscores"));
		assertEquals("string-with-hyphens", StringUtils.chopFirstComponent("string-with-hyphens"));
		assertEquals("string/with/slashes", StringUtils.chopFirstComponent("string/with/slashes"));
	}

	public void testChopFirstComponentReturnsSuffixAfterFirstDot() {
		assertEquals("string.starting.in.dot", StringUtils.chopFirstComponent(".string.starting.in.dot"));
		assertEquals("ending.in.dot.", StringUtils.chopFirstComponent("string.ending.in.dot."));
		assertEquals("", StringUtils.chopFirstComponent("foo."));
		assertEquals("gzip", StringUtils.chopFirstComponent(".gzip"));
		assertEquals("fraudwall.util", StringUtils.chopFirstComponent("com.fraudwall.util"));
	}

	/** {@link StringUtils#parseBoolean(String)} ---------------------------------------------------------------- */

	public void testParseBooleanReturnsFalseForNullArgument() {
		assertFalse(StringUtils.parseBoolean(null));
	}

	public void testParseBooleanReturnsTheCorrectResultForNumericArgument() {
		assertEquals(false, StringUtils.parseBoolean("0"));
		assertEquals(true, StringUtils.parseBoolean("1"));
	}

	public void testParseBooleanReturnsTheCorrectResultForNonNumericArguments() {
		assertEquals(true, StringUtils.parseBoolean("true"));
		assertEquals(true, StringUtils.parseBoolean("TRUE"));
		assertEquals(true, StringUtils.parseBoolean("True"));
		assertEquals(false, StringUtils.parseBoolean("false"));
		assertEquals(false, StringUtils.parseBoolean("FALSE"));
		assertEquals(false, StringUtils.parseBoolean("False"));
	}

	public void testParseBooleanReturnsFalseForBizarreArguments() {
		assertEquals(false, StringUtils.parseBoolean("27"));
		assertEquals(false, StringUtils.parseBoolean("dog"));
	}

	/** {@link StringUtils#booleanToOneZero(boolean)} ----------------------------------------------------------- */

	public void testBooleanToOneZeroReturnsCorrectResult() {
		assertEquals("0", StringUtils.booleanToOneZero(false));
		assertEquals("1", StringUtils.booleanToOneZero(true));
	}

	/** {@link StringUtils#parseChar(String)} ------------------------------------------------------------------- */

	public void testParseCharThrowsIllegalArgumentExceptionOnNullArgument() {
		checkParseCharThrowsIllegalArgumentExceptionOn(null);
	}

	public void testParseCharThrowsIllegalArgumentExceptionOnEmptyString() {
		checkParseCharThrowsIllegalArgumentExceptionOn("");
	}

	public void testParseCharThrowsIllegalArgumentExceptionOnAllWhiteSpaceChars() {
		checkParseCharThrowsIllegalArgumentExceptionOn("  \t  ");
	}

	private void checkParseCharThrowsIllegalArgumentExceptionOn(String s) {
		try {
			StringUtils.parseChar(s);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			assertIsNotBlankException("s", ex);
		}
	}

	public void testParseCharThrowsIllegalArgumentExceptionOnStringWithMultipleCharacters() {
		try {
			StringUtils.parseChar("ab");
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			assertEquals("parseChar: argument string contains multiple characters", ex.getMessage());
		}
	}

	public void testParseCharReturnsFirstCharacterOfOneCharacterStrings() {
		assertEquals('a', StringUtils.parseChar("a"));
		assertEquals('A', StringUtils.parseChar("A"));
		assertEquals('\u1234', StringUtils.parseChar("\u1234"));
	}

	/** {@link StringUtils#parseDbString(String)} -------------------------------------------------------------- */

	public void testParseDbStringReturnsNullForSqlNull() {
		assertNull(StringUtils.parseDbString("NULL"));
		assertNull(StringUtils.parseDbString("\\N"));
	}

	public void testParseDbStringReturnsArgumentForNonSqlNullValues() {
		assertEquals("null", StringUtils.parseDbString("null"));
		assertEquals("ThisIsATest", StringUtils.parseDbString("ThisIsATest"));
	}

	/** {@link StringUtils#stringToDbString(String)} ------------------------------------------------------------ */

	public void testStringToDbStringReturnsNullForNullValue() {
		assertEquals("NULL", StringUtils.stringToDbString(null));
	}

	public void testStringToDbStringReturnsNumericStringForNonNullValue() {
		assertEquals("ThisIsATest", StringUtils.stringToDbString("ThisIsATest"));
	}

	/** {@link StringUtils#bigDecimalToDbString(BigDecimal)} ---------------------------------------------------- */

	public void testBigDecimalToDbStringReturnsNullForNullValue() {
		assertEquals("NULL", StringUtils.bigDecimalToDbString(null));
	}

	public void testBigDecimalToDbStringReturnsNumericStringForNonNullValue() {
		assertEquals("3.14159", StringUtils.bigDecimalToDbString(new BigDecimal("3.14159")));
	}

	/** {@link StringUtils#truncateStringIfNecessary(String, int)} ---------------------------------------------- */

	public void testTruncateStringIfNecessaryTest() {
		String s = "12345";

		try {
			StringUtils.truncateStringIfNecessary(s, -1);
		} catch (IllegalArgumentException ex) {
			// expected case
			assertEquals("N must be non-negative", ex.getMessage());
		}

		assertEquals("", StringUtils.truncateStringIfNecessary(s, 0));
		assertEquals("1", StringUtils.truncateStringIfNecessary(s, 1));
		assertEquals("12", StringUtils.truncateStringIfNecessary(s, 2));
		assertEquals("123", StringUtils.truncateStringIfNecessary(s, 3));
		assertEquals("1...", StringUtils.truncateStringIfNecessary(s, 4));
		assertEquals("12345", StringUtils.truncateStringIfNecessary(s, 5));
		assertEquals(s, StringUtils.truncateStringIfNecessary(s, 10));

		String t = "123456789";
		assertEquals("1...", StringUtils.truncateStringIfNecessary(t, 4));
		assertEquals("123...", StringUtils.truncateStringIfNecessary(t, 6));
		assertEquals("12345...", StringUtils.truncateStringIfNecessary(t, 8));
		assertEquals("123456789", StringUtils.truncateStringIfNecessary(t, 10));

		String u = "xy";
		assertEquals("", StringUtils.truncateStringIfNecessary(u, 0));
		assertEquals("x", StringUtils.truncateStringIfNecessary(u, 1));
		assertEquals("xy", StringUtils.truncateStringIfNecessary(u, 2));
		assertEquals("xy", StringUtils.truncateStringIfNecessary(u, 3));
		assertEquals("xy", StringUtils.truncateStringIfNecessary(u, 4));
	}

	/** {@link StringUtils#hex2string(String)} ------------------------------------------------------------------ */

	public void testHex2StringThrowsIllegalArgumentExceptionOnOddLengthArgument() {
		try {
			StringUtils.hex2string("ab12c");
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			assertEquals("hex string has odd length", ex.getMessage());
		}
	}

	public void testHex2StringThrowsNumberFormatExceptionForIllegalHexValue() {
		try {
			StringUtils.hex2string("123456789abcdefg1234");
			fail();
		} catch (NumberFormatException ex) {
			// expected case
			assertEquals("For input string: \"fg\"", ex.getMessage());
		}
	}

	public void testHex2StringConvertsToCorrectString() {
		assertEquals("cat", StringUtils.hex2string("636174"));
		assertEquals("Anchor Intelligence",
				StringUtils.hex2string("416e63686f7220496e74656c6c6967656e6365"));
		assertEquals("Anchor Intelligence",
				StringUtils.hex2string("416E63686F7220496E74656C6C6967656E6365"));
	}

	/** {@link StringUtils#parseHexLong(String)} ---------------------------------------- */

	public void testParseHexLong() {
		assertEquals(-1663899317937419921L, StringUtils.parseHexLong("E8E8A462D453496F"));
		assertEquals(1721577901100485531L, StringUtils.parseHexLong("17E445FAD041CB9B"));
		assertEquals(255L, StringUtils.parseHexLong("FF"));
		assertEquals(256L, StringUtils.parseHexLong("100"));
	}

	public void testParseHexLongThrowsForNullArgument() {
		try {
			StringUtils.parseHexLong(null);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			assertIsNotNullException("s", ex);
		}
	}

}
