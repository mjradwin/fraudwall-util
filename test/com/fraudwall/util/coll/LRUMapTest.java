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
package com.fraudwall.util.coll;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fraudwall.util.AbstractAnchorTest;
import com.fraudwall.util.coll.LRUMap;
import com.fraudwall.util.coll.LRUSet;


/**
 * Tests the {@link LRUSet} implementation.
 *
 * @author Allan Heydon
 */
public class LRUMapTest extends AbstractAnchorTest {

	// --------------------------------- containsKey

	public void testContainsKeyReturnsFalseForKeyNotInCache() {
		LRUMap<Integer,String> cache = LRUMap.create(3);
		assertFalse(cache.containsKey(0));
		cache.put(1, "foo");
		assertFalse(cache.containsKey(0));
		cache.put(2, "bar");
		assertFalse(cache.containsKey(0));
	}

	public void testContainsKeyReturnsTrueForElementInCache() {
		LRUMap<Integer,String> cache = LRUMap.create(3);
		cache.put(1, "foo");
		cache.put(2, "bar");
		assertTrue(cache.containsKey(1));
		assertTrue(cache.containsKey(2));
	}

	public void testContainsKeyReturnsFalseForElementRemovedFromCache() {
		LRUMap<Integer,String> cache = LRUMap.create(3);
		cache.put(1, "foo");
		cache.put(2, "bar");
		cache.remove(1);
		assertFalse(cache.containsKey(1));
	}

	public void testContainsKeyReturnsFalseForElementEvictedFromCache() {
		LRUMap<Integer,String> cache = LRUMap.create(3);
		cache.put(1, "foo");
		cache.put(2, "bar");
		cache.put(3, "baz");
		cache.put(4, "bam");
		assertFalse(cache.containsKey(1));
	}

	public void testContainsKeyDoesNotCauseElementToBeProtectedFromEviction() {
		LRUMap<Integer,String> cache = LRUMap.create(3);
		cache.put(1, "foo");
		cache.put(2, "bar");
		cache.put(3, "baz");
		assertTrue(cache.containsKey(1));
		cache.put(4, "bam");
		assertFalse(cache.containsKey(1));
		assertTrue(cache.containsKey(2));
	}

	// --------------------------------- get

	public void testGetReturnsNullIfKeyNotInMap() {
		LRUMap<Integer,String> cache = LRUMap.create(3);
		assertNull(cache.get(0));
	}

	public void testGetReturnsValueToWhichKeyIsMapped() {
		LRUMap<Integer,String> cache = LRUMap.create(3);
		cache.put(1, "foo");
		assertEquals("foo", cache.get(1));
	}

	public void testGetCausesElementToBeProtectedFromEviction() {
		LRUMap<Integer,String> cache = LRUMap.create(3);
		cache.put(1, "foo");
		cache.put(2, "bar");
		cache.put(3, "baz");
		cache.get(1);
		cache.put(4, "bam");
		assertTrue(cache.containsKey(1));
		assertFalse(cache.containsKey(2));
	}

	public void testGetReturnsNullIfElementHasBeenEvicted() {
		LRUMap<Integer,String> cache = LRUMap.create(2);
		cache.put(1, "foo");
		cache.put(2, "bar");
		cache.put(3, "baz");
		assertNull(cache.get(1));
	}

	// --------------------------------- put

	public void testPutAddsMapping() {
		LRUMap<Integer,String> cache = LRUMap.create(3);
		cache.put(1, "foo");
		assertTrue(cache.containsKey(1));
	}

	public void testPutReturnsCorrectResult() {
		LRUMap<Integer,String> cache = LRUMap.create(3);
		assertNull(cache.put(1, "foo"));
		assertNull(cache.put(2, "bar"));
		assertEquals("foo", cache.put(1, "baz"));
	}

	public void testPutAddsElementAtFrontOfLruQueue() {
		// fill cache
		LRUMap<Integer,String> cache = LRUMap.create(3);
		cache.put(1, "foo");
		cache.put(2, "bar");
		cache.put(3, "baz");

		// add new element at front of queue
		cache.put(4, "bam");

		// move element 4 back in queue
		cache.get(2);
		cache.get(3);

		// check that element 4 has not been evicted
		assertTrue(cache.containsKey(4));
	}

	public void testPutOfExistingElementReturnsSameValue() {
		LRUMap<Integer,String> cache = LRUMap.create(3);
		assertNull(cache.put(1, "foo"));
		assertEquals("foo", cache.put(1, "foo"));
	}

	public void testPutOfExistingElementDoesChangeItsOrder() {
		// fill cache
		LRUMap<Integer,String> cache = LRUMap.create(3);
		cache.put(1, "foo");
		cache.put(2, "bar");
		cache.put(3, "baz");

		// add element 1 again (the element pending eviction)
		cache.put(1, "foo");

		// add new element at front of queue
		cache.put(4, "bam");

		// check that element 1 was NOT evicted but element 2 was
		assertTrue(cache.containsKey(1));
		assertFalse(cache.containsKey(2));
	}

	// --------------------------------- remove

	public void testRemoveRemovesElementFromSet() {
		LRUMap<Integer,String> cache = LRUMap.create(3);
		cache.put(1, "foo");
		cache.remove(1);
		assertFalse(cache.containsKey(1));
	}

	public void testRemoveReturnsCorrectResult() {
		LRUMap<Integer,String> cache = LRUMap.create(3);
		cache.put(1, "foo");
		cache.put(2, "bar");
		assertNotNull(cache.remove(1));
		assertNull(cache.remove(1));
		assertNull(cache.remove(3));
		assertNotNull(cache.remove(2));
	}

	public void testRemoveMakesRoomForAdditionalElement() {
		// fill cache
		LRUMap<Integer,String> cache = LRUMap.create(3);
		cache.put(1, "foo");
		cache.put(2, "bar");
		cache.put(3, "baz");

		// remove an element
		cache.remove(1);

		// add a new element
		cache.put(4, "bam");

		// check that neither of the other 2 was evicted
		assertTrue(cache.containsKey(2));
		assertTrue(cache.containsKey(3));
	}

	public void testRemoveReturnsValueOfElement() {
		LRUMap<Integer,String> cache = makeCacheWithFiveEntries(5);
		String value = cache.remove(4);
		assertEquals(value, "quux");
	}

	// --------------------------------- clear

	public void testClearDoesNothingWhenCacheIsEmpty() {
		LRUMap<Integer,String> cache = LRUMap.create(3);
		cache.clear();
		assertEquals(0, cache.size());
	}

	public void testClearRemovesAllElementsFromCache() {
		// fill cache
		LRUMap<Integer,String> cache = LRUMap.create(3);
		cache.put(1, "foo");
		cache.put(2, "bar");
		cache.put(3, "baz");

		// clear the cache
		cache.clear();

		// add a new element
		cache.put(4, "bam");

		// check that the first three elements are no longer in the cache
		assertFalse(cache.containsKey(1));
		assertFalse(cache.containsKey(2));
		assertFalse(cache.containsKey(3));
		// check that the new element is in the cache
		assertTrue(cache.containsKey(4));
		assertEquals(1, cache.size());

	}

	// --------------------------------- size

	public void testSizeMatchesNumElementsAdded() {
		LRUMap<Integer,String> cache = makeCacheWithFiveEntries(5);
		assertEquals(5, cache.size());
	}

	public void testSizeMatchesNumElementsAddedAfterEviction() {
		LRUMap<Integer,String> cache = makeCacheWithFiveEntries(4);
		assertEquals(4, cache.size());
	}

	// --------------------------------- entrySet

	public void testEntrySetContainsEveryElement() {
		LRUMap<Integer,String> cache = makeCacheWithFiveEntries(5);
		int[] keys = new int[5];
		String[] values = new String[5];
		int i = 0;
		for (Map.Entry<Integer,String> entry : cache.entrySet()) {
			keys[i] = entry.getKey();
			values[i++] = entry.getValue();
		}
		int[] expectedKeys = new int[] { 1, 2, 3, 4, 5 };
		String[] expectedValues = new String[] { "foo", "bar", "baaz", "quux", "toto" };
		Arrays.sort(keys);
		Arrays.sort(values);
		Arrays.sort(expectedKeys);
		Arrays.sort(expectedValues);
		assertArrayEquals(keys, expectedKeys);
		assertArrayEquals(values, expectedValues);
	}

	public void testEntrySetIsImmutable() {
		LRUMap<Integer,String> cache = makeCacheWithFiveEntries(5);
		for (Map.Entry<Integer,String> entry : cache.entrySet()) {
			try {
				entry.setValue("hello");
				fail();
			} catch (UnsupportedOperationException ex) {
				// expected case
			}
		}
	}


	// --------------------------------- load test

	public void testLruCacheUnderLoad() {
		final int W = 1000; // working set size
		final int N = 10000; // total # of elements
		final List<Integer> workingSet = new ArrayList<Integer>(W);

		// fill cache with working set elements
		LRUMap<Integer,String> cache = LRUMap.create(W + 1);
		for (int i = 0; i < W; i++) {
			assertNull(cache.put(i, "Elt" + i));
			workingSet.add(i);
		}

		// loop over all remaining elements
		for (int i = W; i < N; i++) {
			// access all working set elts in random order
			Collections.shuffle(workingSet);
			for (Integer wsElt: workingSet) {
				cache.get(wsElt);
			}

			// add element i
			cache.put(i, "Elt" + i);

			// check that element i-1 was evicted
			if (i > W) {
				assertFalse(cache.containsKey(i-1));
			}
		}
	}

	// --------------------------------- private helpers

	private LRUMap<Integer,String> makeCacheWithFiveEntries(int maxSize) {
		LRUMap<Integer,String> cache = LRUMap.create(maxSize);
		cache.put(1, "foo");
		cache.put(2, "bar");
		cache.put(3, "baaz");
		cache.put(4, "quux");
		cache.put(5, "toto");
		return cache;
	}
}
