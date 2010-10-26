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
/**
 *
 */
package com.fraudwall.util;

import java.util.Arrays;


/**
 * @author mradwin
 *
 */
public class ExpiringLRUMapTest extends AbstractAnchorTest {
	public void testWorksLikeRegularLRUMap() {
		ExpiringLRUMap<Integer, String> cache = createMapWithCapacity3Insert4Entries(10000L);
		assertEquals(3, cache.size());
		assertFalse(cache.isEmpty());
		assertFalse(cache.containsKey(1));
		assertTrue(cache.containsKey(2));
		assertTrue(cache.containsKey(3));
		assertTrue(cache.containsKey(4));
		assertNull(cache.get(1));
		assertEquals("bar", cache.get(2));
		assertEquals("baaz", cache.get(3));
		assertEquals("quux", cache.get(4));
	}

	public void testEmptyMapSizeIsZero() {
		ExpiringLRUMap<Integer,String> cache = ExpiringLRUMap.create(30, 123456L);
		assertEquals(0, cache.size());
		assertTrue(cache.isEmpty());
	}

	public void testExpiringLRUExpiresElementsWithZeroTimeout() {
		ExpiringLRUMap<Integer, String> cache = createMapWithCapacity3Insert4Entries(0L);
		for (int i = 1; i <= 4; i++) {
			assertFalse(cache.containsKey(i));
			assertNull(cache.get(i));
		}
	}

	public void testExpiringLRUExpiresAllElementsAfterTimeout() throws InterruptedException {
		ExpiringLRUMap<Integer, String> cache = createMapWithCapacity3Insert4Entries(5L);
		Thread.sleep(16L);
		for (int i = 1; i <= 4; i++) {
			assertFalse(cache.containsKey(i));
			assertNull(cache.get(i));
		}
	}

	public void testExpiringLRUExpiresSomeElementsAfterShortSleep() throws InterruptedException {
		final long timeout = 50L;
		ExpiringLRUMap<Integer,String> cache = ExpiringLRUMap.create(3, timeout);
		cache.put(1, "foo");
		Thread.sleep(timeout + 10L);
		cache.put(2, "bar");
		// at this point key 1 should be expired but key 2 should still be in cache
		assertFalse(cache.containsKey(1));
		assertNull(cache.get(1));
		assertTrue(cache.containsKey(2));
		assertEquals("bar", cache.get(2));
		// now make sure that key 2 gets expired
		Thread.sleep(timeout + 10L);
		assertFalse(cache.containsKey(2));
		assertNull(cache.get(2));
	}

	public void testReplaceWithinExpiry() {
		ExpiringLRUMap<Integer,String> cache = ExpiringLRUMap.create(3, 100000L);
		cache.put(1, "foo");
		assertEquals("foo", cache.get(1));
		cache.put(1, "bar");
		assertEquals("bar", cache.get(1));
	}

	public void testReplaceAfterExpiry() throws InterruptedException {
		final long timeout = 5L;
		ExpiringLRUMap<Integer,String> cache = ExpiringLRUMap.create(3, timeout);
		cache.put(1, "foo");
		Thread.sleep(timeout + 10L);
		assertNull(cache.get(1));
		cache.put(1, "bar");
		assertEquals("bar", cache.get(1));
	}

	public void testContainsValue() {
		ExpiringLRUMap<Integer,String> cache = ExpiringLRUMap.create(3, 100000L);
		cache.put(1, "foo");
		assertTrue(cache.containsValue("foo"));
		assertFalse(cache.containsValue("bar"));
	}

	public void testKeySetContainsUnexpiredKeys() {
		ExpiringLRUMap<Integer, String> cache = createMapWithCapacity3Insert4Entries(5L);
		cache.put(5, "toto");
		int[] keys = new int[3];
		int i = 0;
		for (Integer val : cache.keySet()) {
			keys[i++] = val;
		}
		int[] expectedKeys = new int[] { 3, 4, 5 };
		Arrays.sort(keys);
		Arrays.sort(expectedKeys);
		assertArrayEquals(keys, expectedKeys);
	}

	public void testEntrySetIsEmptyAfterExpiry() throws InterruptedException {
		final long timeout = 50L;
		ExpiringLRUMap<Integer, String> cache = createMapWithCapacity3Insert4Entries(timeout);
		assertEquals(3, cache.entrySet().size());
		Thread.sleep(timeout + 10L);
		assertEquals(0, cache.entrySet().size());
	}

	public void testKeySetIsEmptyAfterExpiry() throws InterruptedException {
		final long timeout = 50L;
		ExpiringLRUMap<Integer, String> cache = createMapWithCapacity3Insert4Entries(timeout);
		assertEquals(3, cache.keySet().size());
		Thread.sleep(timeout + 10L);
		assertEquals(0, cache.keySet().size());
	}

	private ExpiringLRUMap<Integer, String> createMapWithCapacity3Insert4Entries(long timeout) {
		ExpiringLRUMap<Integer,String> cache = ExpiringLRUMap.create(3, timeout);
		cache.put(1, "foo");
		cache.put(2, "bar");
		cache.put(3, "baaz");
		cache.put(4, "quux");
		return cache;
	}

}
