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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;


/**
 * Tests the {@link Utilities} implementation.
 *
 * @author Allan Heydon
 */
public class UtilitiesTest extends AbstractPropsTest {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		assertTrue(Utilities.isCalledFromUnitTest());
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		assertTrue(Utilities.isCalledFromUnitTest());
	}

	/** {@link Utilities#isDesktop()} ------------------------------------------------------------------------- */

	public void testIsDesktopReturnsCorrectAnswer() {
		final String[] serverNames = {
			"alaska", "fin", "helios", "maine", "malina", "mithra",
			"prune", "ra", "server1", "yhi", "utu", "eki", "wala",
			"chesapeake", "soy", "tofu", "bacon",
		};
		Set<String> servers = new HashSet<String>();
		for (String name : serverNames) {
			servers.add(name);
		}
		assertEquals(!servers.contains(Utilities.getCurrentHost()), Utilities.isDesktop());
	}

	/** {@link Utilities#isCalledFromUnitTest()} -------------------------------------------------------------- */

	public void testIsCalledFromUnitTestReturnsTrueIfCalledFromUnitTest() {
		assertTrue(Utilities.isCalledFromUnitTest());
	}

	/** {@link Utilities#assertIsCalledOnlyFromUnitTest()} ---------------------------------------------------- */

	public void testAssertIsCalledOnlyFromUnitTestIsNoOpIfCalledFromUnitTest() {
		Utilities.assertIsCalledOnlyFromUnitTest();
	}

	/** {@link Utilities#isServer()} -------------------------------------------------------------------------- */

	public void testIsServerReturnsFalseInUnitTest() {
		assertFalse(Utilities.isServer());
	}

	/** {@link Utilities#getUncompressedFilename(String)} ----------------------------------------------------- */

	public void testGetUncompressedFilename() {
		assertEquals("foo", Utilities.getUncompressedFilename("foo"));
		assertEquals("foo.csv", Utilities.getUncompressedFilename("foo.csv"));
		assertEquals("foo.csv", Utilities.getUncompressedFilename("foo.csv.gz"));
		assertEquals("foo", Utilities.getUncompressedFilename("foo.gz"));
		assertEquals("foo.bz2", Utilities.getUncompressedFilename("foo.bz2"));
		assertEquals("", Utilities.getUncompressedFilename(""));
		assertEquals("", Utilities.getUncompressedFilename(".gz"));
		assertEquals(".", Utilities.getUncompressedFilename("."));
		assertEquals("..", Utilities.getUncompressedFilename(".."));
		assertEquals(".gz.", Utilities.getUncompressedFilename(".gz."));
		assertEquals("gz.", Utilities.getUncompressedFilename("gz."));
	}

	/** {@link Utilities#isCurrentHost(String)} --------------------------------------------------------------- */

	public void testIsCurrentHostReturnsCorrectValues() {
		assertTrue(Utilities.isCurrentHost(Utilities.getCurrentHost()));
		for (InetAddress addr : Utilities.getInetAddresses()) {
			assertTrue(Utilities.isCurrentHost(addr.getHostName()));
		}
		assertFalse(Utilities.isCurrentHost("www.google.com"));
	}

	/** {@link Utilities#stripMillisecondsFromTimestamp(long)} ------------------------------------------------ */

	public void testStripMillisecondsFromTimestampLeavesSentinelAlone() {
		assertEquals(-1L, Utilities.stripMillisecondsFromTimestamp(-1L));
	}

	public void testStripMillisecondsFromTimestampStripsMillisecondsCorrectly() {
		Date dt = makeDate(2009, 2, 3, 4, 5, 6, "UTC");
		assertEquals(dt.getTime(), Utilities.stripMillisecondsFromTimestamp(dt.getTime() + 123));
		assertEquals(dt.getTime(), Utilities.stripMillisecondsFromTimestamp(dt.getTime() + 999));
		assertEquals(dt.getTime(), Utilities.stripMillisecondsFromTimestamp(dt.getTime()));
	}

	/** {@link Utilities#parseDuration(String)} --------------------------------------------------------------- */

	public void testParseDurationReturnsCorrectValueForAllTimeSuffixes() {
		checkParseDuration("2w", 7 * 2 * DateUtils.MILLIS_PER_DAY);
		checkParseDuration("3d", 3 * DateUtils.MILLIS_PER_DAY);
		checkParseDuration("4h", 4 * DateUtils.MILLIS_PER_HOUR);
		checkParseDuration("5H", 5 * DateUtils.MILLIS_PER_HOUR);
		checkParseDuration("6m", 6 * DateUtils.MILLIS_PER_MINUTE);
		checkParseDuration("7s", 7 * DateUtils.MILLIS_PER_SECOND);
		checkParseDuration("1500ms", 1500);
	}

	private void checkParseDuration(String arg, long expResult) {
		assertEquals(expResult, Utilities.parseDuration(arg));
	}

	public void testParseDurationThrowsIfTimeSuffixIsUnknown() {
		checkParseDurationThrows("15t");
		checkParseDurationThrows("2W");
		checkParseDurationThrows("3D");
		checkParseDurationThrows("7S");
		checkParseDurationThrows("1500MS");
	}

	private void checkParseDurationThrows(String arg) {
		try {
			Utilities.parseDuration(arg);
			fail();
		} catch (IllegalArgumentException ex) {
			String errMsg =
				"duration [" + arg + "] must end with a duration element: " +
				"w(eeks), d(ays), h(ours) or H(ours), m(inutes), s(econds), or ms(milliseconds)";
			assertEquals(errMsg, ex.getMessage());
		}
	}

	/** {@link Utilities#ifNull(Object, Object)} -------------------------------------------------------------- */

	public void testIfNullReturnsFirstArgIfNonNull() {
		assertEquals("foo", Utilities.ifNull("foo", "bar"));
		assertEquals(new Integer(1), Utilities.ifNull(1, 2));
	}

	public void testIfNullReturnsSecondArgIfFirstArgIsNull() {
		assertEquals("bar", Utilities.ifNull(null, "bar"));
		assertEquals(new Integer(2), Utilities.ifNull(null, 2));
	}

	/** {@link Utilities#collectionContainsAny(Collection, Object...)} ---------------------------------------- */

	public void testCollectionContainsAnyReturnsTheCorrectResults() {
		Set<String> s = new HashSet<String>();
		s.addAll(Arrays.asList("foo", "bar", "baz"));

		// false results
		assertFalse(Utilities.collectionContainsAny(s));
		assertFalse(Utilities.collectionContainsAny(s, "dog"));
		assertFalse(Utilities.collectionContainsAny(s, "dog", "cat"));

		// true results
		assertTrue(Utilities.collectionContainsAny(s, "foo"));
		assertTrue(Utilities.collectionContainsAny(s, "bar"));
		assertTrue(Utilities.collectionContainsAny(s, "baz"));
		assertTrue(Utilities.collectionContainsAny(s, "foo", "bar"));
		assertTrue(Utilities.collectionContainsAny(s, "foo", "baz"));
		assertTrue(Utilities.collectionContainsAny(s, "baz", "bar", "foo"));
		assertTrue(Utilities.collectionContainsAny(s, "dog", "foo", "cat"));
		assertTrue(Utilities.collectionContainsAny(s, "dog", "foo", "cat", "bar"));
	}
}
