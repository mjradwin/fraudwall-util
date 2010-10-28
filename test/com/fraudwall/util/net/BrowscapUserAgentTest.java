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

import com.fraudwall.util.AbstractPropsTest;
import com.fraudwall.util.FWPropsTest;
import com.fraudwall.util.net.BrowscapUserAgent;


/**
 * Tests the {@link BrowscapUserAgent} implementation.
 * 
 * @author Allan Heydon
 */
public class BrowscapUserAgentTest extends AbstractPropsTest {

	// -------------------------------------------- constructor
	
	public void testConstructorSetsNameAndLineNumber() {
		BrowscapUserAgent bcua = new BrowscapUserAgent("name", 42);
		assertEquals("name", bcua.getName());
		assertEquals(42, bcua.getLineNumber());
	}

	// -------------------------------------------- put
	
	public void testPutConvertsTrueValueTo1() {
		BrowscapUserAgent bcua = new BrowscapUserAgent("name", 42);
		bcua.put("foo", "true");
		assertEquals("1", bcua.get("foo"));
	}

	public void testPutConvertsFalseValueTo0() {
		BrowscapUserAgent bcua = new BrowscapUserAgent("name", 42);
		bcua.put("foo", "false");
		assertEquals("0", bcua.get("foo"));
	}
	
	public void testPutDoesNotConvertValuesOtherThanTrueAndFalse() {
		checkPutDoesNotMapValue("TRUE");
		checkPutDoesNotMapValue("True");
		checkPutDoesNotMapValue("FALSE");
		checkPutDoesNotMapValue("False");
		checkPutDoesNotMapValue("1");
		checkPutDoesNotMapValue("0");
		checkPutDoesNotMapValue("IE");
	}
	
	private void checkPutDoesNotMapValue(String value) {
		BrowscapUserAgent bcua = new BrowscapUserAgent("name", 42);
		bcua.put("foo", value);
		assertEquals(value, bcua.get("foo"));
	}

	// -------------------------------------------- getBooleanValue
	
	public void testGetBooleanValueReturnsFalseIfKeyUndefined() {
		BrowscapUserAgent bcua = new BrowscapUserAgent("name", 42);
		assertFalse(bcua.getBooleanValue("foo"));
	}

	public void testGetBooleanValueReturnsTrueIfKeyMappedToTrueValue() {
		BrowscapUserAgent bcua = new BrowscapUserAgent("name", 42);
		bcua.put("foo", "true");
		assertTrue(bcua.getBooleanValue("foo"));
	}

	public void testGetBooleanValueReturnsFalseIfKeyMappedToNonTrueValue() {
		BrowscapUserAgent bcua = new BrowscapUserAgent("name", 42);
		bcua.put("foo", "false");
		assertFalse(bcua.getBooleanValue("foo"));
	}
	
	public void testGetBooleanValueThrowsIfValueIsNotBoolean() {
		BrowscapUserAgent bcua = new BrowscapUserAgent("name", 42);
		bcua.put("bar", "IE");
		try {
			bcua.getBooleanValue("bar");
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			assertEquals(
				"Value for key \"bar\" is \"IE\", which is neither 0 nor 1",
				ex.getMessage());
		}
	}

	// -------------------------------------------- matches
	
	public void testMatchesIsCaseSensitive() {
		checkMatches("AbCdEfg", "AbCdEfg", true);
		checkMatches("AbCdEfg", "abcdefg", false);
		checkMatches("Mozilla", "mozilla", false);
	}

	public void testMatchesIsCorrectForNamesWithNoWildcards() {
		checkMatches("Ask", "Ask", true);
		checkMatches("Ask", "AskWithSuffix", false);
		checkMatches("Ask", "PrefixWithAsk", false);
		String name = "Mozilla/2.0 (compatible; Ask Jeeves)";
		checkMatches(name, name, true);
		checkMatches(name, name + " foo", false);
	}

	public void testMatchesIsCorrectForNamesWithQuestionMarkWildcards() {
		String name = "Speedy?Spider?(http://www.entireweb.com)";
		checkMatches(name, "Speedy Spider (http://www.entireweb.com)", true);
		checkMatches(name, "Speedy-Spider-(http://www.entireweb.com)", true);
		checkMatches(name, "Speedy.Spider.(http://www.entireweb.com)", true);
		checkMatches(name, "SpeedyASpiderB(http://www.entireweb.com)", true);
		checkMatches(name, "SpeedySpider (http://www.entireweb.com)", false);
		checkMatches(name, "Speedy Spider(http://www.entireweb.com)", false);
		checkMatches(name, "Speedy Spider  (http://www.entireweb.com)", false);
		checkMatches(name, "Speedy  Spider (http://www.entireweb.com)", false);
	}

	public void testMatchesIsCorrectForNamesWithSingleStarWildcard() {
		String name = "ConveraCrawler/*";
		checkMatches(name, "ConveraCrawler/", true);
		checkMatches(name, "ConveraCrawler/a", true);
		checkMatches(name, "ConveraCrawler/aaaaaa", true);
		checkMatches(name, "ConveraCrawler/*", true);
		checkMatches(name + "x", "ConveraCrawler/x", true);
		checkMatches(name + "x", "ConveraCrawler/aaaax", true);
		checkMatches(name + "x", "ConveraCrawler/xxxxx", true);
		checkMatches(name, "ConveraCrawler", false);
		checkMatches(name + "x", "ConveraCrawler/aaaaa", false);
	}

	public void testMatchesIsCorrectForNamesWithMultipleStarWildcard() {
		String name = "*A*B*C*";
		checkMatches(name, "ABC", true);
		checkMatches(name, "aABC", true);
		checkMatches(name, "aaaABC", true);
		checkMatches(name, "AxBC", true);
		checkMatches(name, "AxxxBC", true);
		checkMatches(name, "AByC", true);
		checkMatches(name, "AByyyC", true);
		checkMatches(name, "ABCc", true);
		checkMatches(name, "ABCccc", true);
		checkMatches(name, "aaaAxxxByyyCccc", true);
		checkMatches(name, "aaaxxxByyyCccc", false);
		checkMatches(name, "aaaAxxxyyyCccc", false);
		checkMatches(name, "aaaAxxxByyyccc", false);
	}
	
	public void testMatchesIsCorrectForMixtureOfWildcards() {
		String name = "Eule?Robot*";
		checkMatches(name, "EulerRobot", true);
		checkMatches(name, "EulerRobot 1.0", true);
		checkMatches(name, "EulexRobot2.0", true);
		checkMatches(name, "EuleRobot", false);
		checkMatches(name, "EuleRobot1.0", false);
		checkMatches(name, "EulersRobot", false);
		checkMatches(name, "EulersRobot1.0", false);
	}

	public void testMatchesEscapesAllRegExpMetaCharacters() {
		String name = "foo.bar+baz\\car|baz(foo)x{3}[baz]";
		checkMatches(name, name, true);

		// test escaping of .
		checkMatches(name, "foo bar+baz\\car|baz(foo)x{3}[baz]", false);

		// test escaping of +
		checkMatches(name, "foo.barbaz\\car|baz(foo)x{3}[baz]", false);
		checkMatches(name, "foo.barrrbaz\\car|baz(foo)x{3}[baz]", false);

		// test escaping of \
		checkMatches(name, "foo.bar+bazcar|baz(foo)x{3}[baz]", false);

		// test escaping of |
		checkMatches(name, "foo.bar+baz\\car", false);
		checkMatches(name, "baz(foo)x{3}[baz]", false);

		// test escaping of ( and )
		checkMatches(name, "foo.bar+baz\\car|bazfoox{3}[baz]", false);

		// test escaping of { and }
		checkMatches(name, "foo.bar+baz\\car|baz(foo)xxx[baz]", false);

		// test escaping of [ and ]
		checkMatches(name, "foo.bar+baz\\car|baz(foo)x{3}b", false);
	}

	public void testMatchesObeysAcceptSemiColonForCommaTrue() {
		checkMatchesObeysAcceptSemiColonForComma(true);
	}
	
	public void testMatchesObeysAcceptSemiColonForCommaFalse() {
		checkMatchesObeysAcceptSemiColonForComma(false);
	}
	
	private void checkMatchesObeysAcceptSemiColonForComma(boolean enabled) {
		FWPropsTest.setProperty("useragent.browscap.acceptSemiColonForComma", enabled ? "true" : "false");
		String name = "Mozilla/5.0 (Macintosh; ?; *Mac OS X*; *) AppleWebKit/* (KHTML, like Gecko) Version/4.0* Safari/*";
		checkMatches(name, "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_3; en-us) AppleWebKit/531.22.7 (KHTML, like Gecko)"
			+ " Version/4.0.5 Safari/531.22.7", true);
		checkMatches(name, "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_3; en-us) AppleWebKit/531.22.7 (KHTML; like Gecko)"
			+ " Version/4.0.5 Safari/531.22.7", enabled);
		checkMatches(name, "Mozilla/5.0 (Windows; U; Windows NT 6.1; de-DE) AppleWebKit/525.28 (KHTML, like Gecko)"
			+ " Version/3.2.2 Safari/525.28.1", false);
	}

	private void checkMatches(String name, String candidate, boolean expResult) {
		BrowscapUserAgent bcua = new BrowscapUserAgent(name, 0);
		assertEquals(expResult, bcua.matches(candidate));
	}

	// -------------------------------------------- compareTo
	
	public void testCompareToOrdersFirstByDecreasingLength() {
		checkTotalOrdering("aaaaaaaaaaaaaaaaaa", "zzzzzzzzzz", "aaaaaa", "zzz");
		checkTotalOrdering("Yahoo-MMCrawler*", "Yahoo-MMAudVid*", "Yahoo! Mindset", "Yahoo Pipes*");
	}
	
	public void testCompareToBreaksLengthTiesByAlphabeticalOrdering() {
		checkTotalOrdering("aaaaaaaaaa", "zzzzzzzzzz", "aaaaaa", "zzzzzz");
		checkTotalOrdering("Mozilla/4.0", "Mozilla/5.0", "Mozilla/6.0", "Ask");
	}

	public void testCompareToReturnsZeroForIdenticalBrowscapNames() {
		String n = "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT)";
		BrowscapUserAgent ua1 = new BrowscapUserAgent(n, 0);
		BrowscapUserAgent ua2 = new BrowscapUserAgent(n, 0);
		assertEquals(0, ua1.compareTo(ua2));
		assertEquals(0, ua2.compareTo(ua1));
	}

	private void checkTotalOrdering(String n1, String n2, String n3, String n4) {
		BrowscapUserAgent ua1 = new BrowscapUserAgent(n1, 1);
		BrowscapUserAgent ua2 = new BrowscapUserAgent(n2, 2);
		BrowscapUserAgent ua3 = new BrowscapUserAgent(n3, 3);
		BrowscapUserAgent ua4 = new BrowscapUserAgent(n4, 4);
		assertTrue(ua1.compareTo(ua2) < 0);
		assertTrue(ua2.compareTo(ua1) > 0);
		assertTrue(ua1.compareTo(ua3) < 0);
		assertTrue(ua3.compareTo(ua1) > 0);
		assertTrue(ua1.compareTo(ua4) < 0);
		assertTrue(ua4.compareTo(ua1) > 0);
		assertTrue(ua2.compareTo(ua3) < 0);
		assertTrue(ua3.compareTo(ua2) > 0);
		assertTrue(ua2.compareTo(ua4) < 0);
		assertTrue(ua4.compareTo(ua2) > 0);
		assertTrue(ua3.compareTo(ua4) < 0);
		assertTrue(ua4.compareTo(ua3) > 0);
	}

	// -------------------------------------------- equals
	
	public void testEqualsReturnsTrueForEqualBrowscapUserAgentNames() {
		checkEquals(true, "Ask", "Ask");
		checkEquals(true, "Mozilla/4.0", "Mozilla/4.0");
	}
	
	public void testEqualsReturnsFalseForDifferentBrowscapUserAgentNames() {
		checkEquals(false, "Ask", "Ask*");
		checkEquals(false, "Mozilla/4.0", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT)");
		checkEquals(false, "Y!J-BSC/1.0*", "Y!J-SRD/1.0");
	}

	private void checkEquals(boolean expResult, String n1, String n2) {
		BrowscapUserAgent ua1 = new BrowscapUserAgent(n1, 1);
		BrowscapUserAgent ua2 = new BrowscapUserAgent(n2, 2);
		assertEquals(expResult, ua1.equals(ua2));
		assertEquals(expResult, ua2.equals(ua1));
	}
}
